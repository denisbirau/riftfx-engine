package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.layout.Pane;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;

import java.util.List;

public abstract class AbstractUIContainer<T extends Pane> implements Callable {
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
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 0, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);

        if (lambda == null) {
            throw new RuntimeException(getClass().getSimpleName() + " requires a content block.");
        }

        T container = createContainer();
        applySpacing(container, getDefaultSpacing());
        RendererUtils.applyModifier(container, UITheme.CONTAINER, modifierInstance);

        if (modifierInstance != null && modifierInstance.cssProperties.containsKey("-fx-padding")) {
            try {
                applySpacing(container, Double.parseDouble(modifierInstance.cssProperties.get("-fx-padding").replace("px", "")));
            } catch (NumberFormatException _) {
            }
        }

        RendererUtils.registerComponent(interpreter, container, getClass().getSimpleName());
        interpreter.renderer.pushContainer(container);

        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }
        return null;
    }
}
