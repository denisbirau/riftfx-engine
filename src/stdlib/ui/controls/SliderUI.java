package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.Slider;
import stdlib.ui.state.ReactiveBinding;
import stdlib.ui.core.*;
import stdlib.ui.modifier.ModifierInstance;
import stdlib.ui.state.State;

import java.util.List;

public class SliderUI extends AbstractUIComponent {
    public SliderUI() {
        super(3, 4, "min", "max", "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        double min = InterpreterUtils.getArgument(arguments, 0, Double.class, 0.0);
        double max = InterpreterUtils.getArgument(arguments, 1, Double.class, 100.0);
        State state = InterpreterUtils.getArgument(arguments, 2, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 3, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("Slider requires a state object.");
        }

        Slider slider = new Slider(min, max, state.getValueOrDefault(Double.class, min));
        RendererUtils.applyModifier(slider, UITheme.SLIDER, modifierInstance);
        ReactiveBinding.bindBidirectional(state, slider.valueProperty(), slider, min);

        register(interpreter, slider);
        return null;
    }
}
