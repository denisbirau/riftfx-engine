package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.state.ModifierInstance;

import java.util.List;

public class ScrollPaneUI implements Callable {
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
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 0, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);

        if (lambda == null) {
            throw new RuntimeException("ScrollPane requires a content block.");
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        RendererUtils.applyModifier(scrollPane, "-fx-background-color: transparent;", modifierInstance);

        VBox contentBox = new VBox(5.0);
        scrollPane.setContent(contentBox);

        RendererUtils.registerComponent(interpreter, scrollPane, "ScrollPane");

        interpreter.renderer.pushContainer(contentBox);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }
        return null;
    }
}
