package stdlib;

import error.RuntimeError;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import runtime.Callable;
import runtime.Interpreter;

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
            return 1;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            String content = (String) arguments.getFirst();
            Label label = new Label(content);
            if (interpreter.uiContext.isEmpty()) {
                throw new RuntimeError("Text component must be called inside an UI container.", 0);
            }
            Pane parent = interpreter.uiContext.peek();
            parent.getChildren().add(label);
            return null;
        }
    }
}
