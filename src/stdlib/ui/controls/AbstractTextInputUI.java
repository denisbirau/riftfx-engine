package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.TextInputControl;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.ReactiveBinding;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;
import stdlib.ui.state.State;

import java.util.List;

public abstract class AbstractTextInputUI<T extends TextInputControl> extends AbstractUIComponent {
    public AbstractTextInputUI() {
        super(1, 2, "state", "modifier");
    }

    protected abstract T createNode();

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        State state = InterpreterUtils.getArgument(arguments, 0, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException(getClass().getSimpleName() + " requires a state object.");
        }

        T inputControl = createNode();
        inputControl.setText(state.getValueOrDefault(String.class, ""));
        RendererUtils.applyModifier(inputControl, UITheme.INPUT, modifierInstance);
        ReactiveBinding.bindBidirectional(state, inputControl.textProperty(), inputControl, "");

        register(interpreter, inputControl);
        return null;
    }
}
