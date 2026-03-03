import analysis.Resolver;
import ast.Stmt;
import error.ErrorReporter;
import error.IErrorReporter;
import parsing.Parser;
import parsing.Scanner;
import parsing.Token;
import runtime.GameState;
import runtime.Interpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class Main extends JPanel {
    private static Interpreter globalInterpreter;
    private static final IErrorReporter errorReporter = new ErrorReporter();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of parameters.");
            System.exit(64);
        }

        run(args[0]);
    }

    public static void run(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            String sourceCode = new String(bytes, StandardCharsets.UTF_8);

            Scanner scanner = new Scanner(sourceCode, errorReporter);
            List<Token> tokens = scanner.scan();
            if (errorReporter.hadError()) System.exit(65);

            Parser parser = new Parser(tokens, errorReporter);
            List<Stmt> statements = parser.parse();
            if (errorReporter.hadError()) System.exit(65);

            globalInterpreter = new Interpreter(statements, errorReporter);
            Resolver resolver = new Resolver(globalInterpreter, errorReporter);

            resolver.resolve(statements);
            if (errorReporter.hadError()) System.exit(65);

            globalInterpreter.interpret();
            if (errorReporter.hadError()) System.exit(70);

            SwingUtilities.invokeLater(Main::startGameWindow);
        } catch (IOException e) {
            System.err.println("No such file or directory: " + e.getMessage());
        }
    }

    public static void startGameWindow() {
        JFrame frame = new JFrame("Bachelor Thesis");
        Main gamePanel = new Main();
        frame.add(gamePanel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                GameState.keysPressed.add(KeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                GameState.keysPressed.remove(KeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
            }
        });

        frame.setVisible(true);

        final long[] lastTime = { System.currentTimeMillis() };
        Timer timer = new Timer(16, _ -> {
            long now = System.currentTimeMillis();
            double dt = (now - lastTime[0]) / 1000.0;
            lastTime[0] = now;

            GameState.renderQueue.clear();
            globalInterpreter.callScriptFunction("update", List.of(dt));
            globalInterpreter.callScriptFunction("draw");
            gamePanel.repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        for (Consumer<Graphics> command : GameState.renderQueue) {
            command.accept(g);
        }
    }
}


// Must implement
// TODO: Change Resolver Logic to Direct Linking (Symbols/Pointers instead of map of distances)
// TODO: Change Parser Logic to a more flat approach (Looking into Pratt Parsing)

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