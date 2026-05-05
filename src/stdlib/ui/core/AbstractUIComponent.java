package stdlib.ui.core;

import interpreter.Interpreter;
import javafx.scene.Node;
import stdlib.core.AbstractCallable;

public abstract class AbstractUIComponent extends AbstractCallable {
    public AbstractUIComponent(int minArgs, int maxArgs, String... paramNames) {
        super(minArgs, maxArgs, paramNames);
    }

    protected void register(Interpreter interpreter, Node node) {
        String name = getClass().getSimpleName().replace("UI", "");
        RendererUtils.registerComponent(interpreter, node, name);
    }
}
