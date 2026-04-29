package stdlib;

import error.RuntimeError;
import interpreter.NativeObject;
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
    public static <T> T getArgument(List<Object> arguments, int index, Class<T> tClass, T defaultValue) {
        if (index >= arguments.size() || arguments.get(index) == null) {
            return defaultValue;
        }
        Object argument = arguments.get(index);
        if (!tClass.isInstance(argument)) {
            throw new RuntimeException("Argument at position " + (index + 1) + " must be of type " + tClass.getSimpleName() + ".");
        }
        return tClass.cast(argument);
    }

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
            String title = getArgument(arguments, 0, String.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);
            Callable lambda = getArgument(arguments, 2, Callable.class, null);

            if (title == null) {
                throw new RuntimeException("Window requires a title.");
            }
            if (lambda == null) {
                throw new RuntimeException("Window requires content.");
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            VBox root = new VBox();

            if (modifierInstance != null) {
                root.setStyle(modifierInstance.buildCss());
            }

            interpreter.renderer.pushContainer(root);
            try {
                lambda.call(List.of(), interpreter);
            } catch (RuntimeException e) {
                if (e instanceof RuntimeError runtimeError) {
                    interpreter.errorReporter.report("UI Layout Failed: " + runtimeError.getMessage(), runtimeError.getToken());
                } else {
                    System.err.println("Fatal UI Error: " + e.getMessage());
                }
            } finally {
                interpreter.renderer.popContainer();
            }

            Scene scene = new Scene(root, 400, 300);
            stage.setScene(scene);
            stage.show();

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
            Object contentArgument = arguments.isEmpty() ? null : arguments.getFirst();
            String content = "";

            if (contentArgument instanceof Double d) {
                content = stringify(d);
            } else if (contentArgument instanceof String s) {
                content = s;
            } else if (contentArgument != null) {
                content = contentArgument.toString();
            }

            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);

            Label label = new Label(content);
            if (modifierInstance != null) {
                label.setStyle(modifierInstance.buildCss());
            }

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Text must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(label);
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
            ModifierInstance modifierInstance = getArgument(arguments, 0, ModifierInstance.class, null);
            Callable lambda = getArgument(arguments, 1, Callable.class, null);

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

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException(getClass().getSimpleName() + " must be called inside an UI container.");
            }

            interpreter.renderer.addComponent(container);
            interpreter.renderer.pushContainer(container);

            try {
                lambda.call(List.of(), interpreter);
            } finally {
                interpreter.renderer.popContainer();
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
            String text = getArgument(arguments, 0, String.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);
            Callable lambda = getArgument(arguments, 2, Callable.class, null);

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

            button.setOnAction(_ -> {
                try {
                    lambda.call(List.of(), interpreter);
                } catch (RuntimeException e) {
                    if (e instanceof RuntimeError runtimeError) {
                        interpreter.errorReporter.report("UI Error: " + runtimeError.getMessage(), runtimeError.getToken());
                    } else {
                        System.err.println("Fatal UI Error: " + e.getMessage());
                    }
                }
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Button must be called inside an UI component.");
            }
            interpreter.renderer.addComponent(button);
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
                        listeners.removeIf(uiListener -> !uiListener.update());
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
            State state = getArgument(arguments, 0, State.class, null);
            Callable lambda = getArgument(arguments, 1, Callable.class, null);

            if (state == null) {
                throw new RuntimeException("Observe requires a state object.");
            }
            if (lambda == null) {
                throw new RuntimeException("Observe requires a lambda content block.");
            }

            VBox container = new VBox();
            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Observe must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(container);

            UIListener recompose = () -> {
                // 1. THE LIFECYCLE CHECK
                // If an outer Observe cleared the screen, this container was orphaned.
                // Returning false tells the State object to permanently delete this listener.
                if (container.getParent() == null && container.getScene() == null) {
                    return false;
                }

                // 2. THE RECOMPOSITION
                container.getChildren().clear();
                interpreter.renderer.pushContainer(container);
                try {
                    lambda.call(List.of(), interpreter);
                } catch (RuntimeException e) {
                    if (e instanceof RuntimeError runtimeError) {
                        interpreter.errorReporter.report("Recomposition Failed: " + runtimeError.getMessage(), runtimeError.getToken());
                    } else {
                        System.err.println("Fatal UI Error: " + e.getMessage());
                    }
                } finally {
                    interpreter.renderer.popContainer();
                }

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
            State state = getArgument(arguments, 0, State.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);

            if (state == null) {
                throw new RuntimeException("TextField requires a state object.");
            }

            javafx.scene.control.TextField textField = new javafx.scene.control.TextField();
            textField.setText(state.value != null ? state.value.toString() : "");
            if (modifierInstance != null) {
                textField.setStyle(modifierInstance.buildCss());
            }

            textField.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(state.value)) {
                    state.value = newValue;
                    state.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                // If this text field was removed from the screen, tell State to delete this listener!
                if (textField.getParent() == null && textField.getScene() == null) {
                    return false;
                }

                String newStateValue = state.value != null ? state.value.toString() : "";
                if (!textField.getText().equals(newStateValue)) {
                    textField.setText(newStateValue);
                }
                return true;
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("TextField must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(textField);
            return null;
        }
    }

    public static class ModifierInstance implements NativeObject {
        public final Map<String, String> cssProperties;

        public ModifierInstance() {
            this.cssProperties = new HashMap<>();
        }

        public ModifierInstance(Map<String, String> cssProperties) {
            this.cssProperties = new HashMap<>(cssProperties);
        }

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
                        ModifierInstance newModifier = new ModifierInstance(cssProperties);
                        newModifier.cssProperties.put("-fx-padding", arguments.getFirst().toString() + "px");
                        return newModifier;
                    }
                };
                case "fontSize" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        ModifierInstance newModifier = new ModifierInstance(cssProperties);
                        newModifier.cssProperties.put("-fx-font-size", arguments.getFirst().toString() + "px");
                        return newModifier;
                    }
                };
                case "textColor" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        ModifierInstance newModifier = new ModifierInstance(cssProperties);
                        newModifier.cssProperties.put("-fx-text-fill", arguments.getFirst().toString());
                        return newModifier;
                    }
                };
                case "background" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        ModifierInstance newModifier = new ModifierInstance(cssProperties);
                        newModifier.cssProperties.put("-fx-background-color", arguments.getFirst().toString());
                        return newModifier;
                    }
                };
                case "border" -> new Callable() {
                    @Override
                    public int arity() {
                        return 1;
                    }

                    @Override
                    public Object call(List<Object> arguments, Interpreter interpreter) {
                        ModifierInstance newModifier = new ModifierInstance(cssProperties);
                        newModifier.cssProperties.put("-fx-border-color", arguments.getFirst().toString());
                        newModifier.cssProperties.put("-fx-border-width", "2px");
                        return newModifier;
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
            String text = getArgument(arguments, 0, String.class, "");
            State state = getArgument(arguments, 1, State.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 2, ModifierInstance.class, null);

            if (state == null) {
                throw new RuntimeException("Checkbox requires a state object.");
            }

            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox(text);
            checkBox.setSelected(state.value instanceof Boolean ? (Boolean) state.value : false);

            if (modifierInstance != null) {
                checkBox.setStyle(modifierInstance.buildCss());
            }

            checkBox.selectedProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(state.value)) {
                    state.value = newValue;
                    state.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (checkBox.getParent() == null && checkBox.getScene() == null) {
                    return false;
                }

                boolean newStateValue = state.value instanceof Boolean ? (Boolean) state.value : false;
                if (checkBox.isSelected() != newStateValue) {
                    checkBox.setSelected(newStateValue);
                }

                return true;
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Checkbox must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(checkBox);

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
            double min = getArgument(arguments, 0, Double.class, 0.0);
            double max = getArgument(arguments, 1, Double.class, 100.0);
            State state = getArgument(arguments, 2, State.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 3, ModifierInstance.class, null);

            if (state == null) {
                throw new RuntimeException("Slider requires a state object.");
            }

            javafx.scene.control.Slider slider = new javafx.scene.control.Slider(min, max, state.value instanceof Double ? (Double) state.value : min);

            if (modifierInstance != null) {
                slider.setStyle(modifierInstance.buildCss());
            }

            slider.valueProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(state.value)) {
                    state.value = newValue.doubleValue();
                    state.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (slider.getParent() == null && slider.getScene() == null) {
                    return false;
                }

                double newStateValue = state.value instanceof Double ? (Double) state.value : min;
                if (slider.getValue() != newStateValue) {
                    slider.setValue(newStateValue);
                }
                return true;
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Slider must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(slider);
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
            String url = getArgument(arguments, 0, String.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);

            if (url == null) {
                throw new RuntimeException("Image requires an URL or file path.");
            }

            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(url));
            imageView.setPreserveRatio(true);

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Image must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(imageView);

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

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("Spacer must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(spacer);
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
            State state = getArgument(arguments, 0, State.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);

            if (state == null) {
                throw new RuntimeException("PasswordField requires a state object.");
            }

            javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
            passwordField.setText(state.value != null ? state.value.toString() : "");

            if (modifierInstance != null) {
                passwordField.setStyle(modifierInstance.buildCss());
            }

            passwordField.textProperty().addListener((_, _, newValue) -> {
                if (!newValue.equals(state.value)) {
                    state.value = newValue;
                    state.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (passwordField.getParent() == null && passwordField.getScene() == null) {
                    return false;
                }

                String newStateValue = state.value != null ? state.value.toString() : "";
                if (!passwordField.getText().equals(newStateValue)) {
                    passwordField.setText(newStateValue);
                }
                return true;
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("PasswordField must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(passwordField);
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
            State state = getArgument(arguments, 0, State.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);

            if (state == null) {
                throw new RuntimeException("ProgressBar requires a state object.");
            }

            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
            progressBar.setProgress(state.value instanceof Double ? (Double) state.value : 0.0);

            if (modifierInstance != null) {
                progressBar.setStyle(modifierInstance.buildCss());
            }

            state.listeners.add(() -> {
                if (progressBar.getParent() == null && progressBar.getScene() == null) {
                    return false;
                }

                double newStateValue = state.value instanceof Double ? (Double) state.value : 0.0;
                if (progressBar.getProgress() != newStateValue) {
                    progressBar.setProgress(newStateValue);
                }
                return true;
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("ProgressBar must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(progressBar);

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
            ModifierInstance modifierInstance = getArgument(arguments, 0, ModifierInstance.class, null);
            Callable lambda = getArgument(arguments, 1, Callable.class, null);

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

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("ScrollPane must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(scrollPane);

            interpreter.renderer.pushContainer(contentBox);
            try {
                lambda.call(List.of(), interpreter);
            } finally {
                interpreter.renderer.popContainer();
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
            State state = getArgument(arguments, 0, State.class, null);
            ModifierInstance modifierInstance = getArgument(arguments, 1, ModifierInstance.class, null);

            List<String> options = new ArrayList<>();
            for (int i = 2; i < arguments.size(); i++) {
                Object arg = arguments.get(i);
                if (arg instanceof String s) {
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

            comboBox.valueProperty().addListener((_, _, newValue) -> {
                if (newValue != null && !newValue.equals(state.value)) {
                    state.value = newValue;
                    state.listeners.removeIf(uiListener -> !uiListener.update());
                }
            });

            state.listeners.add(() -> {
                if (comboBox.getParent() == null && comboBox.getScene() == null) {
                    return false;
                }

                String newStateValue = state.value != null ? state.value.toString() : "";
                if (!newStateValue.equals(comboBox.getValue()) && options.contains(newStateValue)) {
                    comboBox.setValue(newStateValue);
                }
                return true;
            });

            if (interpreter.renderer.isEmpty()) {
                throw new RuntimeException("ComboBox must be called inside an UI container.");
            }
            interpreter.renderer.addComponent(comboBox);

            return null;
        }
    }
}
