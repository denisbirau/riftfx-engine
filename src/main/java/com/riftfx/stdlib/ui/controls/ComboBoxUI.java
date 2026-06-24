package com.riftfx.stdlib.ui.controls;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.ComboBox;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.state.ReactiveBinding;
import com.riftfx.stdlib.ui.core.*;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;
import com.riftfx.stdlib.ui.state.State;

import java.util.ArrayList;
import java.util.List;

public class ComboBoxUI extends AbstractUIComponent {
    public ComboBoxUI() {
        super(1, 255, "state", "modifier", "options...");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        State state = InterpreterUtils.getArgument(arguments, 0, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("ComboBox requires a state object.");
        }

        List<String> options = new ArrayList<>();
        for (int i = 2; i < arguments.size(); i++) {
            Object arg = arguments.get(i);
            if (arg instanceof String s) {
                options.add(s);
            } else if (arg instanceof List<?> l) {
                l.forEach(elem -> options.add(elem.toString()));
            }
        }

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(options);

        if (state.value != null && options.contains(state.value.toString())) {
            comboBox.setValue(state.value.toString());
        } else if (!options.isEmpty()) {
            comboBox.setValue(options.getFirst());
            state.value = options.getFirst();
        }

        RendererUtils.applyModifier(comboBox, UITheme.COMBO_BOX, modifierInstance);

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ReactiveBinding.notifyStateChanged(state, newValue);
            }
        });

        ReactiveBinding.bindStateListener(comboBox, state, () -> {
            String expected = state.getValueOrDefault(String.class, "");
            if (!expected.equals(comboBox.getValue()) && options.contains(expected)) {
                comboBox.setValue(expected);
            }
        });

        register(interpreter, comboBox);
        return null;
    }
}
