package stdlib;

import error.RuntimeError;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import runtime.Callable;
import runtime.Interpreter;

import java.util.ArrayList;
import java.util.List;

public class NativeUI {
    public static class Window implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String title = (String) arguments.get(0);
            Callable lambda = (Callable) arguments.get(1);
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                VBox root = new VBox();
                interpreter.uiContext.push(root);
                try {
                    lambda.call(List.of(), interpreter);
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
            return -1;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (arguments.isEmpty()) {
                throw new RuntimeError("Text component requires at least one argument.", 0);
            }
            String content = (String) arguments.getFirst();
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
                throw new RuntimeError("Text component must be called inside an UI container.", 0);
            }
            Pane parent = interpreter.uiContext.peek();
            parent.getChildren().add(label);
            return null;
        }
    }

    public static class Column implements Callable {
        @Override
        public int arity() {
            return -1;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (arguments.isEmpty()) {
                throw new RuntimeError("Column component must have at least one argument.", 0);
            }
            Callable lambda;
            VBox column = new VBox();
            if (arguments.size() == 2) {
                Double spacing = (Double) arguments.getFirst();
                column.setSpacing(spacing);
                column.setStyle("-fx-padding: " + spacing + "px;");
                lambda = (Callable) arguments.get(1);
            } else {
                column.setSpacing(5);
                lambda = (Callable) arguments.getFirst();
            }
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeError("Column must be called inside an UI container.", 0);
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
            return -1;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            if (arguments.isEmpty()) {
                throw new RuntimeError("Row component must have at least one argument.", 0);
            }
            Callable lambda;
            HBox row = new HBox();
            if (arguments.size() == 2) {
                Double spacing = (Double) arguments.getFirst();
                row.setSpacing(spacing);
                row.setStyle("-fx-padding: " + spacing + "px;");
                lambda = (Callable) arguments.get(1);
            } else {
                row.setSpacing(10);
                lambda = (Callable) arguments.getFirst();
            }
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeError("Row must be called inside an UI container.", 0);
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
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String text = (String) arguments.get(0);
            Callable lambda = (Callable) arguments.get(1);
            javafx.scene.control.Button button = new javafx.scene.control.Button(text);
            button.setOnAction(_ -> lambda.call(List.of(), interpreter));
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeError("Button should be called inside an UI component.", 0);
            }
            interpreter.uiContext.peek().getChildren().add(button);
            return null;
        }
    }

    public static class State {
        public Object value;
        public final List<Runnable> listeners = new ArrayList<>();

        public State(Object value) {
            this.value = value;
        }
    }

    public static class CreateState implements Callable {
        @Override
        public int arity() {
            return 1;
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
        public Object call(List<Object> arguments, Interpreter interpreter) {
            State state = (State) arguments.get(0);
            Callable lambda = (Callable) arguments.get(1);
            VBox container = new VBox();
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeError("Observe must be called inside an UI container.", 0);
            }
            interpreter.uiContext.peek().getChildren().add(container);
            Runnable recompose = () -> Platform.runLater(() -> {
                container.getChildren().clear();
                interpreter.uiContext.push(container);
                try {
                    lambda.call(List.of(), interpreter);
                } finally {
                    interpreter.uiContext.pop();
                }
            });
            state.listeners.add(recompose);
            recompose.run();
            return null;
        }
    }
}
