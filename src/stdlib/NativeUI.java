package stdlib;

import error.ErrorReporter;
import error.RuntimeError;
import interpreter.NativeObject;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import interpreter.Callable;
import interpreter.Interpreter;
import scanner.Token;

import java.util.ArrayList;
import java.util.List;

public class NativeUI {
    public static class Window implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("title", "content");
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (!(arguments.get(0) instanceof String title)) {
                throw new RuntimeException("Window title must be a string.");
            }
            if (!(arguments.get(1) instanceof Callable lambda)) {
                throw new RuntimeException("Window requires a lambda content block.");
            }
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                VBox root = new VBox();
                interpreter.uiContext.push(root);
                try {
                    lambda.call(List.of(), interpreter);
                } catch (RuntimeException e) {
                    if (e instanceof RuntimeError runtimeError) {
                        ErrorReporter.report("UI Layout Failed: " + runtimeError.getMessage(), runtimeError.getToken());
                    } else {
                        System.err.println("Fatal UI Error: " + e.getMessage());
                    }
                } finally {
                    interpreter.uiContext.pop();
                }
                Scene scene = new Scene(root, 400, 300);
                stage.setScene(scene);
                stage.show();
            });
            return null;
        }
    }

    public static class Text implements Callable {
        @Override
        public int arity() {
            return 3;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("content", "fontSize", "color");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (arguments.isEmpty()) {
                throw new RuntimeException("Text requires content.");
            }
            String content = arguments.getFirst().toString();
            Label label = new Label(content);
            StringBuilder css = new StringBuilder();

            if (arguments.size() > 1 && arguments.get(1) instanceof Double fontSize) {
                css.append("-fx-font-size: ").append(fontSize).append("px; ");
            }
            if (arguments.size() > 2 && arguments.get(2) instanceof String color) {
                css.append("-fx-text-fill: ").append(color).append("; ");
            }
            label.setStyle(css.toString());

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Text must be called inside an UI container.");
            }
            Pane parent = interpreter.uiContext.peek();
            parent.getChildren().add(label);
            return null;
        }
    }

    public static class Column implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("spacing", "content");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            double spacing = 0.5; // Default spacing
            Callable lambda = null;

            for (Object arg : arguments) {
                if (arg instanceof Double d) {
                    spacing = d;
                } else if (arg instanceof Callable callable) {
                    lambda = callable;
                }
            }
            if (lambda == null) {
                throw new RuntimeException("Column requires a lambda content block.");
            }

            VBox column = new VBox();
            column.setSpacing(spacing);
            column.setStyle("-fx-padding: " + spacing + "px;");

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Column must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(column);
            interpreter.uiContext.push(column);
            try {
                lambda.call(List.of(), interpreter);
            } finally {
                interpreter.uiContext.pop();
            }
            return null;
        }
    }

    public static class Row implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("spacing", "content");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            double spacing = 10.0; // Default spacing
            Callable lambda = null;

            for (Object arg : arguments) {
                if (arg instanceof Double d) {
                    spacing = d;
                } else if (arg instanceof Callable callable) {
                    lambda = callable;
                }
            }

            if (lambda == null) {
                throw new RuntimeException("Row requires a lambda content block.");
            }

            HBox row = new HBox();
            row.setSpacing(spacing);
            row.setStyle("-fx-padding: " + spacing + "px;");

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Row must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(row);
            interpreter.uiContext.push(row);
            try {
                lambda.call(List.of(), interpreter);
            } finally {
                interpreter.uiContext.pop();
            }
            return null;
        }
    }

    public static class Button implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("text", "onClick");
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (!(arguments.get(0) instanceof String text)) {
                throw new RuntimeException("Button requires a text string.");
            }
            if (!(arguments.get(1) instanceof Callable lambda)) {
                throw new RuntimeException("Button requires an onClick lambda.");
            }
            javafx.scene.control.Button button = new javafx.scene.control.Button(text);
            button.setOnAction(_ -> {
                try {
                    lambda.call(List.of(), interpreter);
                } catch (RuntimeException e) {
                    System.err.println("UI Error: " + e.getMessage());
                }
            });
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Button must be called inside an UI component.");
            }
            interpreter.uiContext.peek().getChildren().add(button);
            return null;
        }
    }

    public interface UIListener {
        boolean update(); // Returns true if the update was successful, false if the UI element is dead
    }

    public static class State implements NativeObject {
        public Object value;
        public final List<UIListener> listeners = new ArrayList<>();

        public State(Object value) {
            this.value = value;
        }

        @Override
        public Object getMember(Token member) {
            if (member.lexeme().equals("get")) {
                return new Callable() {
                    @Override
                    public int arity() {
                        return 0;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        return value;
                    }
                };
            }
            if (member.lexeme().equals("set")) {
                return new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public List<String> parameterNames() {
                        return List.of("value");
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        value = arguments.getFirst();
                        listeners.removeIf(listener -> !listener.update());
                        return null;
                    }
                };
            }
            throw new RuntimeException("Undefined property: " + member.lexeme() + ".");
        }

        @Override
        public void setMember(Token member, Object value) {
            throw new RuntimeException("Cannot directly set properties on a State object. Use .set()");
        }
    }

    public static class CreateState implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("initialValue");
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            return new State(arguments.getFirst());
        }
    }

    public static class Observe implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("state", "content");
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (!(arguments.get(0) instanceof State state)) {
                throw new RuntimeException("Observe requires a state object.");
            }
            if (!(arguments.get(1) instanceof Callable lambda)) {
                throw new RuntimeException("Observe requires a lambda content block.");
            }

            VBox container = new VBox();
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Observe must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(container);

            UIListener recompose = () -> {
                // 1. THE LIFECYCLE CHECK
                // If an outer Observe cleared the screen, this container was orphaned.
                // Returning false tells the State object to permanently delete this listener.
                if (container.getParent() == null && container.getScene() == null) {
                    return false;
                }

                // 2. THE RECOMPOSITION
                Platform.runLater(() -> {
                    container.getChildren().clear();
                    interpreter.uiContext.push(container);
                    try {
                        lambda.call(List.of(), interpreter);
                    } catch (RuntimeException e) {
                        if (e instanceof RuntimeError runtimeError) {
                            ErrorReporter.report("Recomposition Failed: " + runtimeError.getMessage(), runtimeError.getToken());
                        } else {
                            System.err.println("Fatal UI Error: " + e.getMessage());
                        }
                    } finally {
                        interpreter.uiContext.pop();
                    }
                });

                return true; // Still alive, keep listening!
            };
            state.listeners.add(recompose);
            recompose.update();
            return null;
        }
    }

    public static class TextField implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("state");
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (!(arguments.getFirst() instanceof State state)) {
                throw new RuntimeException("TextField requires state object.");
            }

            javafx.scene.control.TextField textField = new javafx.scene.control.TextField();
            textField.setText(state.value != null ? state.value.toString() : "");

            textField.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(state.value)) {
                    state.value = newValue;
                    state.listeners.removeIf(listener -> !listener.update());
                }
            });

            state.listeners.add(() -> {
                // If this text field was removed from the screen, tell State to delete this listener!
                if (textField.getParent() == null && textField.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    String newStateValue = state.value != null ? state.value.toString() : "";
                    if (!textField.getText().equals(newStateValue)) {
                        textField.setText(newStateValue);
                    }
                });
                return true;
            });
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("TextField must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(textField);
            return null;
        }
    }
}
