package app;

import interpreter.JavaFXRenderer;
import interpreter.UIRenderer;
import resolution.Resolver;
import ast.Stmt;
import error.ErrorReporter;
import parser.Parser;
import scanner.Scanner;
import scanner.Token;
import interpreter.Interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javafx.application.Platform;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of parameters.");
            System.exit(64);
        }

        compileAndRun(args[0]);
    }

    public static void compileAndRun(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            String sourceCode = new String(bytes, StandardCharsets.UTF_8);

            Platform.startup(() -> {});

            ErrorReporter errorReporter = new ErrorReporter();

            Scanner scanner = new Scanner(sourceCode, errorReporter);
            List<Token> tokens = scanner.scan();
            if (errorReporter.hadError()) exit(65);

            Parser parser = new Parser(tokens, errorReporter);
            List<Stmt> statements = parser.parse();
            if (errorReporter.hadError()) exit(65);

            Resolver resolver = new Resolver(errorReporter);
            resolver.resolve(statements);
            if (errorReporter.hadError()) exit(65);

            UIRenderer renderer = new JavaFXRenderer();
            Interpreter interpreter = new Interpreter(statements, errorReporter, renderer);

            Platform.runLater(() -> {
                interpreter.interpret();
                if (errorReporter.hadError()) {
                    Platform.exit();
                    exit(70);
                }
            });
        } catch (IOException e) {
            System.err.println("No such file or directory: " + e.getMessage());
        }
    }

    private static void exit(int code) {
        Platform.exit();
        System.exit(code);
    }
}
