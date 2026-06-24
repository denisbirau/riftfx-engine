package com.riftfx.stdlib.ui.layout;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.riftfx.stdlib.ui.core.AbstractUIComponent;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.RendererUtils;
import com.riftfx.stdlib.ui.core.UITheme;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class ScrollPaneUI extends AbstractUIComponent {
    public ScrollPaneUI() {
        super(1, 2, "modifier", "content");
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
        RendererUtils.applyModifier(scrollPane, UITheme.SCROLL_PANE, modifierInstance);

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
