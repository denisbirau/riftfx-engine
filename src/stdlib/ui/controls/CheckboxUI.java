package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.CheckBox;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.ReactiveBinding;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;
import stdlib.ui.state.State;

import java.util.List;

public class CheckboxUI extends AbstractUIComponent {
    public CheckboxUI() {
        super(2, 3, "text", "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String text = InterpreterUtils.getArgument(arguments, 0, String.class, "");
        State state = InterpreterUtils.getArgument(arguments, 1, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 2, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("Checkbox requires a state object.");
        }

        CheckBox checkBox = new CheckBox(text);
        checkBox.setSelected(state.getValueOrDefault(Boolean.class, false));
        RendererUtils.applyModifier(checkBox, UITheme.TEXT, modifierInstance);
        ReactiveBinding.bindBidirectional(state, checkBox.selectedProperty(), checkBox, false);

        register(interpreter, checkBox);
        return null;
    }
}
