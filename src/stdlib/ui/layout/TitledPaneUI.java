package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;

import java.util.List;

public class TitledPaneUI implements Callable {
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
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, "Group");
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);

        if (lambda == null) {
            throw new RuntimeException("TitledPane requires a content block.");
        }

        VBox paneContent = new VBox(10);
        paneContent.setStyle("-fx-padding: 10px;");

        interpreter.renderer.pushContainer(paneContent);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }

        TitledPane titledPane = new TitledPane(title, paneContent);
        titledPane.setExpanded(true);
        titledPane.setCollapsible(true);

        RendererUtils.applyModifier(titledPane, UITheme.CONTAINER, modifierInstance);

        RendererUtils.registerComponent(interpreter, titledPane, "TitledPane");
        return null;
    }
}
