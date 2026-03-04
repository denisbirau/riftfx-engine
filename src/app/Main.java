package app;

import compiler.Resolver;
import ast.Stmt;
import engine.GameWindow;
import error.ErrorReporter;
import error.IErrorReporter;
import compiler.Parser;
import compiler.Scanner;
import compiler.Token;
import runtime.Interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final IErrorReporter errorReporter = new ErrorReporter();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of parameters.");
            System.exit(64);
        }

        Interpreter interpreter = compileAndRun(args[0]);

        if (interpreter != null) {
            GameWindow gameWindow = new GameWindow(interpreter);
            gameWindow.start();
        }
    }

    public static Interpreter compileAndRun(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            String sourceCode = new String(bytes, StandardCharsets.UTF_8);

            Scanner scanner = new Scanner(sourceCode, errorReporter);
            List<Token> tokens = scanner.scan();
            if (errorReporter.hadError()) System.exit(65);

            Parser parser = new Parser(tokens, errorReporter);
            List<Stmt> statements = parser.parse();
            if (errorReporter.hadError()) System.exit(65);

            Resolver resolver = new Resolver(errorReporter);
            resolver.resolve(statements);
            if (errorReporter.hadError()) System.exit(65);

            Interpreter interpreter = new Interpreter(statements, errorReporter);
            interpreter.interpret();
            if (errorReporter.hadError()) System.exit(70);

            return interpreter;
        } catch (IOException e) {
            System.err.println("No such file or directory: " + e.getMessage());
        }
        return null;
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