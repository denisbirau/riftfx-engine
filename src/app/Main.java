package app;

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

            Scanner scanner = new Scanner(sourceCode);
            List<Token> tokens = scanner.scan();
            if (ErrorReporter.hadError()) exit(65);

            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();
            if (ErrorReporter.hadError()) exit(65);

            Resolver resolver = new Resolver();
            resolver.resolve(statements);
            if (ErrorReporter.hadError()) exit(65);

            Interpreter interpreter = new Interpreter(statements);
            interpreter.interpret();
            if (ErrorReporter.hadError()) exit(70);

            Platform.runLater(() -> {
                if (javafx.stage.Window.getWindows().isEmpty()) {
                    Platform.exit();
                    System.exit(0);
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

// I probably need this
// TODO: Collections (Lists and Maps)
// TODO: Lambda Functions

// Maybe too easy to even bother
// TODO: Static methods and properties
// TODO: Getters and Setters
// TODO: Const Variables
// TODO: String Interpolation
// TODO: Better Input / Output
// TODO: Standard Library (Functions and Classes)
// TODO: Modules
// TODO: Shorthand Assignment Operators (+=, -=, *=, /= ...)
// TODO: Warnings (ex: A variable was not used.)
// TODO: Continue Statement

// Harder todos
// TODO: IDE Support (Language Server Protocol)
// TODO: JIT/AOT (Look into Truffle, GraalVM)

// Game changers
// TODO: Maybe switch to Static Typed Language
// TODO: Maybe switch to Compiler
// TODO: Maybe switch the backend programming language(Scala/C/Rust)
// TODO: Maybe switch to DSL(ex: GUI, Game Engine)

/* GUI idea:

    Window(title: "Thesis Demo", width: 800, height: 600) {
        VerticalLayout {
            Text(value: "Hello, " + user.name, size: 24)

            Button(
                text: "Click Me",
                onClick: () => {
                    user.name = "Professor"; // Changing state updates the UI automatically
                }
            )
        }
    }
*/

/* Game Engine idea:

    entity Player {
        sprite: "hero.png"
        x: 100
        y: 100

        onUpdate() {
            if (Input.isPressed("right")) this.x += 5
        }
    }
*/