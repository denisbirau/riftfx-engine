package stdlib.ui.navigation.tab;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import stdlib.ui.core.*;
import stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class TabPaneUI extends AbstractUIComponent {
    public static final ScopedContext<TabPane> TAB_CONTEXT = new ScopedContext<>();

    public TabPaneUI() {
        super(1, 2, "modifier", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 0, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);
        if (lambda == null) {
            throw new RuntimeException("TabPane requires a content block.");
        }

        TabPane tabPane = new TabPane();
        RendererUtils.applyModifier(tabPane, UITheme.TAB, modifierInstance);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        TAB_CONTEXT.push(tabPane);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            TAB_CONTEXT.pop();
        }

        register(interpreter, tabPane);
        return null;
    }
}
