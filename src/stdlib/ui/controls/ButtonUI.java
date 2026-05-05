package stdlib.ui.controls;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Button;
import stdlib.ui.core.AbstractUIComponent;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class ButtonUI extends AbstractUIComponent {
    public ButtonUI() {
        super(2, 3, "text", "modifier", "onClick");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String text = InterpreterUtils.getArgument(arguments, 0, String.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);
        if (text == null) {
            throw new RuntimeException("Button requires a text.");
        }
        if (lambda == null) {
            throw new RuntimeException("Button requires an onClick.");
        }

        Button button = new Button(text);
        RendererUtils.applyModifier(button, UITheme.BUTTON, modifierInstance);
        button.setOnAction(_ -> InterpreterUtils.executeSafe(interpreter, lambda, List.of(), "UI Interaction"));

        register(interpreter, button);
        return null;
    }
}
