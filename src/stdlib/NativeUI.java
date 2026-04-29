package stdlib;

import error.RuntimeError;
import interpreter.NativeObject;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
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
                        interpreter.errorReporter.report("UI Layout Failed: " + runtimeError.getMessage(), runtimeError.getToken());
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

    public abstract static class AbstractUIContainer<T extends Pane> implements Callable {
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

        protected abstract T createContainer();
        protected abstract void applySpacing(T container, double spacing);
        protected abstract double getDefaultSpacing();

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
                throw new RuntimeException(getClass().getSimpleName() + " requires a content block.");
            }

            T container = createContainer();
            applySpacing(container, getDefaultSpacing());
            if (modifierInstance != null) {
                container.setStyle(modifierInstance.buildCss());
                if (modifierInstance.cssProperties.containsKey("-fx-padding")) {
                    String padding = modifierInstance.cssProperties.get("-fx-padding").replace("px", "");
                    try {
                        applySpacing(container, Double.parseDouble(padding));
                    } catch (NumberFormatException _) {}
                }
            }

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException(getClass().getSimpleName() + " must be called inside an UI container.");
            }

            interpreter.uiContext.peek().getChildren().add(container);
            interpreter.uiContext.push(container);

            try {
                lambda.call(List.of(), interpreter);
            } finally {
                interpreter.uiContext.pop();
            }

            return null;
        }
    }

    public static class Column extends AbstractUIContainer<VBox> {
        @Override
        protected VBox createContainer() {
            return new VBox();
        }

        @Override
        protected void applySpacing(VBox container, double spacing) {
            container.setSpacing(spacing);
        }

        @Override
        protected double getDefaultSpacing() {
            return 5.0;
        }
    }

    public static class Row extends AbstractUIContainer<HBox> {
        @Override
        protected HBox createContainer() {
            return new HBox();
        }

        @Override
        protected void applySpacing(HBox container, double spacing) {
            container.setSpacing(spacing);
        }

        @Override
        protected double getDefaultSpacing() {
            return 10.0;
        }
    }

    public static class Stack extends AbstractUIContainer<StackPane> {
        @Override
        protected StackPane createContainer() {
            return new StackPane();
        }

        @Override
        protected void applySpacing(StackPane container, double spacing) {

        }

        @Override
        protected double getDefaultSpacing() {
            return 0.0;
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
                            interpreter.errorReporter.report("Recomposition Failed: " + runtimeError.getMessage(), runtimeError.getToken());
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

    public static class Checkbox implements Callable {
        @Override
        public int arity() {
            return 3;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("text", "state", "modifier");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 2 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String text = "";
            State state = null;
            ModifierInstance modifierInstance = null;

            for (Object arg : arguments) {
                if (arg instanceof String s) {
                    text = s;
                } else if (arg instanceof State s) {
                    state = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                }
            }

            if (state == null) {
                throw new RuntimeException("Checkbox requires a state object.");
            }

            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox(text);
            checkBox.setSelected(state.value instanceof Boolean ? (Boolean) state.value : false);

            if (modifierInstance != null) {
                checkBox.setStyle(modifierInstance.buildCss());
            }

            State finalState = state;
            checkBox.selectedProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(finalState.value)) {
                    finalState.value = newValue;
                    finalState.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (checkBox.getParent() == null && checkBox.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    boolean newStateValue = finalState.value instanceof Boolean ? (Boolean) finalState.value : false;
                    if (checkBox.isSelected() != newStateValue) {
                        checkBox.setSelected(newStateValue);
                    }
                });
                return true;
            });

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Checkbox must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(checkBox);

            return null;
        }
    }

    public static class Slider implements Callable {
        @Override
        public int arity() {
            return 4;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("min", "max", "state", "modifier");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 3 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            double min = 0;
            double max = 100;
            State state = null;
            ModifierInstance modifierInstance = null;

            int doubleCount = 0;
            for (Object arg : arguments) {
                if (arg instanceof Double d) {
                    if (doubleCount == 0) {
                        min = d;
                    } else if (doubleCount == 1) {
                        max = d;
                    }
                    doubleCount++;
                } else if (arg instanceof State s) {
                    state = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                }
            }

            if (state == null) {
                throw new RuntimeException("Slider requires a state object.");
            }

            javafx.scene.control.Slider slider = new javafx.scene.control.Slider(min, max, state.value instanceof Double ? (Double) state.value : min);

            if (modifierInstance != null) {
                slider.setStyle(modifierInstance.buildCss());
            }

            State finalState = state;

            slider.valueProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(finalState.value)) {
                    finalState.value = newValue.doubleValue();
                    finalState.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            double finalMin = min;
            state.listeners.add(() -> {
                if (slider.getParent() == null && slider.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    double newStateValue = finalState.value instanceof Double ? (Double) finalState.value : finalMin;
                    if (slider.getValue() != newStateValue) {
                        slider.setValue(newStateValue);
                    }
                });
                return true;
            });

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Slider must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(slider);

            return null;
        }
    }

    public static class Image implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("url", "modifier");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1 && argCount <= arity();
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String url = null;
            ModifierInstance modifierInstance = null;

            for (Object arg : arguments) {
                if (arg instanceof String s) {
                    url = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                }
            }

            if (url == null) {
                throw new RuntimeException("Image requires an URL or file path.");
            }

            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(url));
            imageView.setPreserveRatio(true);

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Image must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(imageView);

            return null;
        }
    }

    public static class Spacer implements Callable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            Pane spacer = new Pane();

            HBox.setHgrow(spacer, Priority.ALWAYS);
            VBox.setVgrow(spacer, Priority.ALWAYS);

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("Spacer must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(spacer);

            return null;
        }
    }

    public static class PasswordField implements Callable {
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
                throw new RuntimeException("PasswordField requires a state object.");
            }

            javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
            passwordField.setText(state.value != null ? state.value.toString() : "");

            if (modifierInstance != null) {
                passwordField.setStyle(modifierInstance.buildCss());
            }

            State finalState = state;
            passwordField.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(finalState.value)) {
                    finalState.value = newValue;
                    finalState.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (passwordField.getParent() == null && passwordField.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    String newStateValue = finalState.value != null ? finalState.value.toString() : "";
                    if (!passwordField.getText().equals(newStateValue)) {
                        passwordField.setText(newStateValue);
                    }
                });
                return true;
            });

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("PasswordField must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(passwordField);

            return null;
        }
    }

    public static class ProgressBar implements Callable {
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
                throw new RuntimeException("ProgressBar requires a state object.");
            }

            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
            progressBar.setProgress(state.value instanceof Double ? (Double) state.value : 0.0);

            if (modifierInstance != null) {
                progressBar.setStyle(modifierInstance.buildCss());
            }

            State finalState = state;
            state.listeners.add(() -> {
                if (progressBar.getParent() == null && progressBar.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    double newStateValue = finalState.value instanceof Double ? (Double) finalState.value : 0.0;
                    if (progressBar.getProgress() != newStateValue) {
                        progressBar.setProgress(newStateValue);
                    }
                });
                return true;
            });

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("ProgressBar must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(progressBar);

            return null;
        }
    }

    public static class ScrollPane implements Callable {
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
                throw new RuntimeException("ScrollPane requires a content block.");
            }

            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            if (modifierInstance != null) {
                scrollPane.setStyle(scrollPane.getStyle() + modifierInstance.buildCss());
            }

            VBox contentBox = new VBox();
            contentBox.setSpacing(5.0);
            scrollPane.setContent(contentBox);

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("ScrollPane must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(scrollPane);

            interpreter.uiContext.push(contentBox);
            try {
                lambda.call(List.of(), interpreter);
            } finally {
                interpreter.uiContext.pop();
            }

            return null;
        }
    }

    public static class ComboBox implements Callable {
        @Override
        public int arity() {
            return 255;
        }

        @Override
        public List<String> parameterNames() {
            return List.of("state", "modifier", "options...");
        }

        @Override
        public boolean acceptsArity(int argCount) {
            return argCount >= 1;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            State state = null;
            ModifierInstance modifierInstance = null;
            List<String> options = new ArrayList<>();

            for (Object arg : arguments) {
                if (arg instanceof State s) {
                    state = s;
                } else if (arg instanceof ModifierInstance m) {
                    modifierInstance = m;
                } else if (arg instanceof String s) {
                    options.add(s);
                } else if (arg instanceof List<?> l) {
                    for (Object elem : l) {
                        options.add(elem.toString());
                    }
                }
            }

            if (state == null) {
                throw new RuntimeException("ComboBox requires a state object.");
            }

            javafx.scene.control.ComboBox<String> comboBox = new javafx.scene.control.ComboBox<>();
            comboBox.getItems().addAll(options);

            if (state.value != null && options.contains(state.value.toString())) {
                comboBox.setValue(state.value.toString());
            } else if (!options.isEmpty()) {
                comboBox.setValue(options.getFirst());
                state.value = options.getFirst();
            }

            if (modifierInstance != null) {
                comboBox.setStyle(modifierInstance.buildCss());
            }

            State finalState = state;
            comboBox.valueProperty().addListener((_, _, newValue) -> {
                if (newValue != null && !newValue.equals(finalState.value)) {
                    finalState.value = newValue;
                    finalState.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (comboBox.getParent() == null && comboBox.getScene() == null) {
                    return false;
                }
                Platform.runLater(() -> {
                    String newStateValue = finalState.value != null ? finalState.value.toString() : "";
                    if (!newStateValue.equals(comboBox.getValue()) && options.contains(newStateValue)) {
                        comboBox.setValue(newStateValue);
                    }
                });
                return true;
            });

            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeException("ComboBox must be called inside an UI container.");
            }
            interpreter.uiContext.peek().getChildren().add(comboBox);

            return null;
        }
    }
}
