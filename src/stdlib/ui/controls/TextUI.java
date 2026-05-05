package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.Label;
import stdlib.ui.core.AbstractUIComponent;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class TextUI extends AbstractUIComponent {
    public TextUI() {
        super(1, 2, "content", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Object contentArgument = arguments.isEmpty() ? null : arguments.getFirst();
        String content = "";
        if (contentArgument instanceof Double d) {
            content = stringify(d);
        } else if (contentArgument != null) {
            content = contentArgument.toString();
        }
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);

        Label label = new Label(content);
        RendererUtils.applyModifier(label, UITheme.TEXT, modifierInstance);

        register(interpreter, label);
        return null;
    }

    private String stringify(Double d) {
        String str = d.toString();
        return str.endsWith(".0") ? str.substring(0, str.length() - 2) : str;
    }
}
