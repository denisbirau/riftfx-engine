package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import stdlib.NativeArray;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;

import java.util.List;

public class TabPaneUI implements Callable {
    @Override
    public int arity() {
        return 2;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("tabs", "modifier");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount >= 1 && argCount <= arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Object tabsArgument = InterpreterUtils.getArgument(arguments, 0, Object.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);

        TabPane tabPane = new TabPane();
        RendererUtils.applyModifier(tabPane, UITheme.TAB, modifierInstance);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        if (tabsArgument instanceof NativeArray(List<Object> elements)) {
            elements.forEach(item -> {
                if (item instanceof Tab t) {
                    tabPane.getTabs().add(t);
                } else {
                    throw new RuntimeException("TabPane sequenceExpression must only contain Tab objects.");
                }
            });
        } else {
            throw new RuntimeException("TabPane requires an sequenceExpression of Tabs.");
        }

        RendererUtils.registerComponent(interpreter, tabPane, "TabPane");
        return null;
    }
}
