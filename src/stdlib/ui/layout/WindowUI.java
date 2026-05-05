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

        Stage stage = new Stage();
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

        stage.setScene(new Scene(root, UITheme.WINDOW_WIDTH, UITheme.WINDOW_HEIGHT));
        stage.show();
        return null;
    }
}
