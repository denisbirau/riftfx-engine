package stdlib.ui.controls;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.Node;
import stdlib.ui.core.RendererUtils;

import java.util.List;

public abstract class AbstractUIComponent implements Callable {
    private final int minArgs;
    private final int maxArgs;
    private final List<String> paramNames;

    public AbstractUIComponent(int minArgs, int maxArgs, String... paramNames) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.paramNames = List.of(paramNames);
    }

    @Override
    public int arity() {
        return maxArgs;
    }

    @Override
    public List<String> parameterNames() {
        return paramNames;
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount >= minArgs && argCount <= maxArgs;
    }

    protected void register(Interpreter interpreter, Node node) {
        String name = getClass().getSimpleName().replace("UI", "");
        RendererUtils.registerComponent(interpreter, node, name);
    }
}
