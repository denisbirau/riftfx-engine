package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import stdlib.ui.core.InterpreterUtils;

import java.util.List;

public class TabUI implements Callable {
    @Override
    public int arity() {
        return 2;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("title", "content");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount >= 1 && argCount <= arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, "Tab");
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);

        if (lambda == null) {
            throw new RuntimeException("Tab requires a content block.");
        }

        Tab tab = new Tab(title);
        tab.setClosable(false);

        VBox tabContent = new VBox(10);
        VBox.setVgrow(tabContent, Priority.ALWAYS);
        tab.setContent(tabContent);

        interpreter.renderer.pushContainer(tabContent);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }
        return tab;
    }
}
