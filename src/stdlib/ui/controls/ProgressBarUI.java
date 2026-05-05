package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.ProgressBar;
import stdlib.ui.state.ReactiveBinding;
import stdlib.ui.core.*;
import stdlib.ui.modifier.ModifierInstance;
import stdlib.ui.state.State;

import java.util.List;

public class ProgressBarUI extends AbstractUIComponent {
    public ProgressBarUI() {
        super(1, 2, "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        State state = InterpreterUtils.getArgument(arguments, 0, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("ProgressBar requires a state object.");
        }

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(state.getValueOrDefault(Double.class, 0.0));
        RendererUtils.applyModifier(progressBar, UITheme.PROGRESS, modifierInstance);
        ReactiveBinding.bindStateListener(progressBar, state, () -> {
            double expected = state.getValueOrDefault(Double.class, 0.0);
            if (progressBar.getProgress() != expected) {
                progressBar.setProgress(expected);
            }
        });

        register(interpreter, progressBar);
        return null;
    }
}
