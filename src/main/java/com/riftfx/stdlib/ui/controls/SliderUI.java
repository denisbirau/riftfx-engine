package com.riftfx.stdlib.ui.controls;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.Slider;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.state.ReactiveBinding;
import com.riftfx.stdlib.ui.core.*;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;
import com.riftfx.stdlib.ui.state.State;

import java.util.List;

public class SliderUI extends AbstractUIComponent {
    public SliderUI() {
        super(3, 5, "min", "max", "state", "modifier", "step");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        double min = InterpreterUtils.getArgument(arguments, 0, Double.class, 0.0);
        double max = InterpreterUtils.getArgument(arguments, 1, Double.class, 100.0);
        State state = InterpreterUtils.getArgument(arguments, 2, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 3, ModifierInstance.class, null);
        Double step = InterpreterUtils.getArgument(arguments, 4, Double.class, null);
        if (state == null) {
            throw new RuntimeException("Slider requires a state object.");
        }

        Slider slider = new Slider(min, max, state.getValueOrDefault(Double.class, min));
        RendererUtils.applyModifier(slider, UITheme.SLIDER, modifierInstance);

        if (step != null) {
            slider.setMajorTickUnit(step);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
        }

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                double val = newValue.doubleValue();

                if (step != null) {
                    val = Math.round(val / step) * step;
                }

                val = Math.round(val * 100.0) / 100.0;

                ReactiveBinding.notifyStateChanged(state, val);
            }
        });

        ReactiveBinding.bindStateListener(slider, state, () -> {
            double expected = state.getValueOrDefault(Double.class, min);
            if (Math.abs(slider.getValue() - expected) > 0.0001) { // Floating point safe comparison
                slider.setValue(expected);
            }
        });

        register(interpreter, slider);
        return null;
    }
}
