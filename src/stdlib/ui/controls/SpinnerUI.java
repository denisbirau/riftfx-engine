package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.Spinner;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.ReactiveBinding;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;
import stdlib.ui.state.State;

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
        RendererUtils.applyModifier(spinner, UITheme.INPUT, modifierInstance);
        ReactiveBinding.bindBidirectional(state, spinner.getValueFactory().valueProperty(), spinner, min);

        register(interpreter, spinner);
        return null;
    }
}
