package stdlib.ui.navigation.menu;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import stdlib.core.AbstractCallable;
import stdlib.ui.core.InterpreterUtils;

import java.util.List;

public class MenuItemUI extends AbstractCallable {
    public MenuItemUI() {
        super(2, 2, "title", "onClick");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, "Item");
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);

        if (lambda == null) {
            throw new RuntimeException("MenuItem requires an onClick block.");
        }

        MenuItem menuItem = new MenuItem(title);
        menuItem.setOnAction(_ -> InterpreterUtils.executeSafe(interpreter, lambda, List.of(), "Menu Action"));

        Object parent = MenuBarUI.MENU_CONTEXT.peek();
        if (parent instanceof Menu parentMenu) {
            parentMenu.getItems().add(menuItem);
        } else {
            throw new RuntimeException("MenuItem must be placed inside a Menu.");
        }
        return menuItem;
    }
}
