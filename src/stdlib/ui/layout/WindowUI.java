package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import stdlib.core.AbstractCallable;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class WindowUI extends AbstractCallable {
    private static Stage stage = null;

    public WindowUI() {
        super(2, 3, "title", "modifier", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);
        if (title == null) {
            throw new RuntimeException("Window requires a title.");
        }
        if (lambda == null) {
            throw new RuntimeException("Window requires content.");
        }

        if (stage == null) {
            stage = new Stage();
            stage.setOnCloseRequest(_ -> System.exit(0));
        }
        stage.setTitle(title);
        VBox root = new VBox();
        RendererUtils.applyModifier(root, UITheme.ROOT, modifierInstance);

        interpreter.renderer.pushContainer(root);
        try {
            lambda.call(List.of(), interpreter);
        } catch (RuntimeException e) {
            InterpreterUtils.reportError(interpreter, e, "UI Layout");
        } finally {
            interpreter.renderer.popContainer();
        }

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, UITheme.WINDOW_WIDTH, UITheme.WINDOW_HEIGHT));
        } else {
            stage.getScene().setRoot(root);
        }
        stage.show();
        return null;
    }
}
