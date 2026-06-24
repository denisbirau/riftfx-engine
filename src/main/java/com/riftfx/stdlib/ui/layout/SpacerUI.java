package com.riftfx.stdlib.ui.layout;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.riftfx.stdlib.ui.core.AbstractUIComponent;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.RendererUtils;
import com.riftfx.stdlib.ui.core.UITheme;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class SpacerUI extends AbstractUIComponent {
    public SpacerUI() {
        super(0, 1, "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 0, ModifierInstance.class, null);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        RendererUtils.applyModifier(spacer, UITheme.SPACER, modifierInstance);

        register(interpreter, spacer);
        return null;
    }
}
