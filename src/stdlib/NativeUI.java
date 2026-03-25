package stdlib;

import javafx.application.Platform;
import javafx.scene.Scene;
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
                // TODO: push root on Implicit Parent Stack
                lambda.call(List.of(), interpreter);
                // TODO: pop root
                Scene scene = new Scene(root, 400, 300);
                stage.setScene(scene);
                stage.show();
            });
            return null;
        }
    }
}
