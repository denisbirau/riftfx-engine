package com.riftfx.app;

import com.riftfx.interpreter.JavaFXRenderer;
import com.riftfx.interpreter.UIRenderer;
import com.riftfx.resolution.Resolver;
import com.riftfx.ast.Stmt;
import com.riftfx.error.ErrorReporter;
import com.riftfx.parser.Parser;
import com.riftfx.scanner.Scanner;
import com.riftfx.scanner.Token;
import com.riftfx.interpreter.Interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import javafx.application.Platform;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of parameters.");
            System.exit(64);
        }
        String path = args[0];
        Platform.startup(() -> {
        });
        compileAndRun(path);
        startHotReloadWatcher(path);
    }

    public static void compileAndRun(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            String sourceCode = new String(bytes, StandardCharsets.UTF_8);

            ErrorReporter errorReporter = new ErrorReporter();

            Scanner scanner = new Scanner(sourceCode, errorReporter);
            List<Token> tokens = scanner.scan();
            if (errorReporter.hadError()) {
                return;
            }

            Parser parser = new Parser(tokens, errorReporter);
            List<Stmt> statements = parser.parse();
            if (errorReporter.hadError()) {
                return;
            }

            Resolver resolver = new Resolver(errorReporter);
            resolver.resolve(statements);
            if (errorReporter.hadError()) {
                return;
            }

            Platform.runLater(() -> {
                try {
                    UIRenderer renderer = new JavaFXRenderer();
                    Interpreter interpreter = new Interpreter(statements, errorReporter, renderer);
                    interpreter.interpret();
                } catch (Exception e) {
                    System.err.println("Runtime exception: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("No such file or directory: " + e.getMessage());
        }
    }

    private static void startHotReloadWatcher(String path) {
        Thread watcherThread = new Thread(() -> {
            try {
                Path file = Paths.get(path).toAbsolutePath();
                Path dir = file.getParent();

                WatchService watchService = FileSystems.getDefault().newWatchService();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                System.out.println("Hot Reload active on: " + file.getFileName());

                while (true) {
                    WatchKey watchKey = watchService.take();

                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        Path changedFile = (Path) event.context();
                        if (changedFile.equals(file.getFileName())) {
                            Thread.sleep(50);
                            System.out.println("Recompiling...");
                            compileAndRun(path);
                        }
                    }
                    watchKey.reset();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Watcher error: " + e.getMessage());
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }
}
