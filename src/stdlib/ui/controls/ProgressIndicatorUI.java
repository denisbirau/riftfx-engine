package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.ProgressIndicator;
import stdlib.ui.state.ReactiveBinding;
import stdlib.ui.core.*;
import stdlib.ui.modifier.ModifierInstance;
import stdlib.ui.state.State;

import java.util.List;

public class ProgressIndicatorUI extends AbstractUIComponent {
    public ProgressIndicatorUI() {
        super(1, 2, "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        State state = InterpreterUtils.getArgument(arguments, 0, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("ProgressIndicator requires a state object.");
        }

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(state.getValueOrDefault(Double.class, -1.0)); // -1.0 = indeterminate spinning animation
        RendererUtils.applyModifier(progressIndicator, UITheme.PROGRESS, modifierInstance);
        ReactiveBinding.bindStateListener(progressIndicator, state, () -> {
            double expected = state.getValueOrDefault(Double.class, -1.0);
            if (progressIndicator.getProgress() != expected) {
                progressIndicator.setProgress(expected);
            }
        });

        register(interpreter, progressIndicator);
        return null;
    }
}
