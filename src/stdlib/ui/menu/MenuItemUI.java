package stdlib.ui.menu;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.MenuItem;
import stdlib.ui.core.InterpreterUtils;

import java.util.List;

public class MenuItemUI implements Callable {
    @Override
    public int arity() {
        return 2;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("title", "onClick");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount == arity();
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
        return menuItem;
    }
}
