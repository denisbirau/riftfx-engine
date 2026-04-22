package stdlib;

import error.ErrorReporter;
import error.RuntimeError;
import interpreter.NativeObject;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import interpreter.Callable;
import interpreter.Interpreter;
import scanner.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeUI {
    public static class Window implements Callable {
        @Override
        public int arity() {
            return 3;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("title", "modifier", "content");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 2 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String title = null;
            ModifierInstance modifierInstance = null;
            Callable lambda = null;

            for (Object arg : arguments) {
                if (arg instanceof String s) {
                    title = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                } else if (arg instanceof Callable c) {
                    lambda = c;
                }
            }

            if (title == null) {
                throw new RuntimeException("Window requires a title.");
            }
            if (lambda == null) {
                throw new RuntimeException("Window requires content.");
            }

            final String finalTitle = title;
            final ModifierInstance finalModifierInstance = modifierInstance;
            final Callable finalLambda = lambda;

            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(finalTitle);
                VBox root = new VBox();

                if (finalModifierInstance != null) {
                    root.setStyle(finalModifierInstance.buildCss());
                }

                interpreter.uiContext.push(root);
                try {
                    finalLambda.call(List.of(), interpreter);
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
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("content", "modifier");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String content = "";
            ModifierInstance modifierInstance = null;

            for (Object arg : arguments) {
                if (arg instanceof String s) {
                    content = s;
                } else if (arg instanceof Double d) {
                    content = stringify(d);
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                }
            }

            Label label = new Label(content);
            if (modifierInstance != null) {
                label.setStyle(modifierInstance.buildCss());
            }

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Text must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(label);
            return null;
        }

        private String stringify(Double d) {
            String str = d.toString();
            return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
        }
    }

    public static class Column implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("modifier", "content");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            ModifierInstance modifierInstance = null;
            Callable lambda = null;

            for (Object arg : arguments) {
                if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                } else if (arg instanceof Callable c) {
                    lambda = c;
                }
            }
            if (lambda == null) {
                throw new RuntimeException("Column requires content.");
            }

            VBox column = new VBox();
            column.setSpacing(5);
            if (modifierInstance != null) {
                column.setStyle(modifierInstance.buildCss());
                if (modifierInstance.cssProperties.containsKey("-fx-padding")) {
                    var padding = modifierInstance.cssProperties.get("-fx-padding").replace("px", "");
                    try {
                        column.setSpacing(Double.parseDouble(padding));
                    } catch (Exception _) {
                    }
                }
            }

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
            return List.of("modifier", "content");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            ModifierInstance modifierInstance = null;
            Callable lambda = null;

            for (Object arg : arguments) {
                if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                } else if (arg instanceof Callable c) {
                    lambda = c;
                }
            }

            if (lambda == null) {
                throw new RuntimeException("Row requires content.");
            }

            HBox row = new HBox();
            row.setSpacing(10);
            if (modifierInstance != null) {
                row.setStyle(modifierInstance.buildCss());
                if (modifierInstance.cssProperties.containsKey("-fx-padding")) {
                    var padding = modifierInstance.cssProperties.get("-fx-padding").replace("px", "");
                    try {
                        row.setSpacing(Double.parseDouble(padding));
                    } catch (Exception _) {
                    }
                }
            }

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
            return 3;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("text", "modifier", "onClick");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 2 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String text = null;
            ModifierInstance modifierInstance = null;
            Callable lambda = null;

            for (Object arg : arguments) {
                if (arg instanceof String s) {
                    text = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                } else if (arg instanceof Callable c) {
                    lambda = c;
                }
            }

            if (text == null) {
                throw new RuntimeException("Button requires a text.");
            }
            if (lambda == null) {
                throw new RuntimeException("Button requires an onClick.");
            }

            javafx.scene.control.Button button = new javafx.scene.control.Button(text);
            if (modifierInstance != null) {
                button.setStyle(modifierInstance.buildCss());
            }

            Callable finalLambda = lambda;
            button.setOnAction(_ -> {
                try {
                    finalLambda.call(List.of(), interpreter);
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
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("state", "modifier");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            State state = null;
            ModifierInstance modifierInstance = null;

            for (Object arg : arguments) {
                if (arg instanceof State s) {
                    state = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                }
            }

            if (state == null) {
                throw new RuntimeException("TextField requires a state object.");
            }

            javafx.scene.control.TextField textField = new javafx.scene.control.TextField();
            textField.setText(state.value != null ? state.value.toString() : "");
            if (modifierInstance != null) {
                textField.setStyle(modifierInstance.buildCss());
            }

            State finalState = state;
            textField.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(finalState.value)) {
                    finalState.value = newValue;
                    finalState.listeners.removeIf(listener -> !listener.update());
                }
            });

            state.listeners.add(() -> {
                // If this text field was removed from the screen, tell State to delete this listener!
                if (textField.getParent() == null && textField.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    String newStateValue = finalState.value != null ? finalState.value.toString() : "";
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

    public static class ModifierInstance implements NativeObject {
        public final Map<String, String> cssProperties = new HashMap<>();

        @Override
        public Object getMember(Token member) {
            return switch (member.lexeme()) {
                case "padding" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        cssProperties.put("-fx-padding", arguments.getFirst().toString() + "px");
                        return ModifierInstance.this;
                    }
                };
                case "fontSize" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        cssProperties.put("-fx-font-size", arguments.getFirst().toString() + "px");
                        return ModifierInstance.this;
                    }
                };
                case "textColor" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        cssProperties.put("-fx-text-fill", arguments.getFirst().toString());
                        return ModifierInstance.this;
                    }
                };
                case "background" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        cssProperties.put("-fx-background-color", arguments.getFirst().toString());
                        return ModifierInstance.this;
                    }
                };
                case "border" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        cssProperties.put("-fx-border-color", arguments.getFirst().toString());
                        cssProperties.put("-fx-border-width", "2px");
                        return ModifierInstance.this;
                    }
                };
                default -> throw new RuntimeException("Unknown Modifier member: '" + member.lexeme() + "'.");
            };
        }

        @Override
        public void setMember(Token member, Object value) {
            throw new RuntimeException("Modifiers are immutable.");
        }

        public String buildCss() {
            StringBuilder css = new StringBuilder();
            cssProperties.forEach((key, value) -> css.append(key).append(": ").append(value).append("; "));
            return css.toString();
        }
    }

    public static class ModifierBase implements NativeObject {
        @Override
        public Object getMember(Token member) {
            ModifierInstance modifierInstance = new ModifierInstance();
            return modifierInstance.getMember(member);
        }

        @Override
        public void setMember(Token member, Object value) {
            throw new RuntimeException("Modifiers are immutable.");
        }
    }
}
