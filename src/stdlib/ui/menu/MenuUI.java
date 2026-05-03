package stdlib.ui.menu;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import stdlib.NativeArray;
import stdlib.ui.core.InterpreterUtils;

import java.util.List;

public class MenuUI implements Callable {
    @Override
    public int arity() {
        return 2;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("title", "items");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount == arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, "Menu");
        Object itemsArgument = InterpreterUtils.getArgument(arguments, 1, Object.class, null);

        Menu menu = new Menu(title);

        if (itemsArgument instanceof NativeArray(List<Object> elements)) {
            elements.forEach(item -> {
                if (item instanceof MenuItem mi) {
                    menu.getItems().add(mi);
                } else {
                    throw new RuntimeException("Menu sequenceExpression must ony contain MenuItem objects.");
                }
            });
        } else {
            throw new RuntimeException("Menu requires an sequenceExpression of MenuItems.");
        }
        return menu;
    }
}
