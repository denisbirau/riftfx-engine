package stdlib.ui.menu;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import stdlib.NativeArray;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;

import java.util.List;

public class MenuBarUI implements Callable {
    @Override
    public int arity() {
        return 2;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("menus", "modifier");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount >= 1 && argCount <= arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Object menusArgument = InterpreterUtils.getArgument(arguments, 0, Object.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);

        MenuBar menuBar = new MenuBar();

        RendererUtils.applyModifier(menuBar, UITheme.MENU, modifierInstance);

        if (menusArgument instanceof NativeArray(List<Object> elements)) {
            elements.forEach(item -> {
                if (item instanceof Menu m) {
                    menuBar.getMenus().add(m);
                } else {
                    throw new RuntimeException("MenuBar sequenceExpression must only contain Menu objects.");
                }
            });
        } else {
            throw new RuntimeException("MenuBar requires an sequenceExpression of Menus.");
        }

        RendererUtils.registerComponent(interpreter, menuBar, "MenuBar");
        return null;
    }
}
