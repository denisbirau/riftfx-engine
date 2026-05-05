package com.riftfx.stdlib.ui.controls;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.RadioButton;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.state.ReactiveBinding;
import com.riftfx.stdlib.ui.core.*;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;
import com.riftfx.stdlib.ui.state.State;

import java.util.List;
import java.util.Objects;

public class RadioButtonUI extends AbstractUIComponent {
    public RadioButtonUI() {
        super(3, 4, "text", "optionValue", "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String text = InterpreterUtils.getArgument(arguments, 0, String.class, "");
        Object optionValue = InterpreterUtils.getArgument(arguments, 1, Object.class, null);
        State state = InterpreterUtils.getArgument(arguments, 2, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 3, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("RadioButton requires a state object.");
        }

        RadioButton radioButton = new RadioButton(text);
        radioButton.setSelected(Objects.equals(state.value, optionValue));
        RendererUtils.applyModifier(radioButton, UITheme.RADIO, modifierInstance);

        radioButton.setOnAction(event -> {
            if (radioButton.isSelected()) {
                ReactiveBinding.notifyStateChanged(state, optionValue);
            }
        });

        ReactiveBinding.bindStateListener(radioButton, state, () -> {
            boolean shouldBeSelected = Objects.equals(state.value, optionValue);
            if (radioButton.isSelected() != shouldBeSelected) {
                radioButton.setSelected(shouldBeSelected);
            }
        });

        register(interpreter, radioButton);
        return null;
    }
}
