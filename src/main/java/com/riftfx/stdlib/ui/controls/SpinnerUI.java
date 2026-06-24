package com.riftfx.stdlib.ui.controls;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.Spinner;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.state.ReactiveBinding;
import com.riftfx.stdlib.ui.core.*;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;
import com.riftfx.stdlib.ui.state.State;

import java.util.List;

public class SpinnerUI extends AbstractUIComponent {
    public SpinnerUI() {
        super(3, 4, "min", "max", "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        double min = InterpreterUtils.getArgument(arguments, 0, Double.class, 0.0);
        double max = InterpreterUtils.getArgument(arguments, 1, Double.class, 100.0);
        State state = InterpreterUtils.getArgument(arguments, 2, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 3, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("Spinner requires a state object.");
        }

        Spinner<Double> spinner = new Spinner<>(min, max, state.getValueOrDefault(Double.class, min));
        spinner.setEditable(true);
        RendererUtils.applyModifier(spinner, UITheme.SPINNER, modifierInstance);
        ReactiveBinding.bindBidirectional(state, spinner.getValueFactory().valueProperty(), spinner, min);

        register(interpreter, spinner);
        return null;
    }
}
