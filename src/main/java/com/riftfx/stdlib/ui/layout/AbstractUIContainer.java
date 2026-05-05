package com.riftfx.stdlib.ui.layout;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.layout.Pane;
import com.riftfx.stdlib.ui.core.AbstractUIComponent;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.RendererUtils;
import com.riftfx.stdlib.ui.core.UITheme;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public abstract class AbstractUIContainer<T extends Pane> extends AbstractUIComponent {
    public AbstractUIContainer(int minArgs, int maxArgs, String... paramNames) {
        super(minArgs, maxArgs, paramNames);
    }

    protected abstract T createContainer();

    protected abstract void applySpacing(T container, double spacing);

    protected abstract double getDefaultSpacing();

    protected void renderContent(Interpreter interpreter, Pane container, Callable lambda) {
        interpreter.renderer.pushContainer(container);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }
    }

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
                applySpacing(container,
                        Double.parseDouble(modifierInstance.cssProperties.get("-fx-padding").replace("px", "")));
            } catch (NumberFormatException e) {
            }
        }

        RendererUtils.registerComponent(interpreter, container, getClass().getSimpleName());
        renderContent(interpreter, container, lambda);
        return null;
    }
}
