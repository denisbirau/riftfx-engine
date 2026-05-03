package stdlib.ui.layout;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;

import java.util.List;

public class GridCellUI implements Callable {
    @Override
    public int arity() {
        return 3;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("column", "row", "content");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount == arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        double col = InterpreterUtils.getArgument(arguments, 0, Double.class, 0.0);
        double row = InterpreterUtils.getArgument(arguments, 1, Double.class, 0.0);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);

        if (lambda == null) {
            throw new RuntimeException("GridCell requires a content block.");
        }

        Object currentParent = interpreter.renderer.peekContainer();
        if (!(currentParent instanceof GridPane)) {
            throw new RuntimeException("GridCell must be placed directly inside a Grid.");
        }

        StackPane cellContainer = new StackPane();
        GridPane.setColumnIndex(cellContainer, (int) col);
        GridPane.setRowIndex(cellContainer, (int) row);

        RendererUtils.registerComponent(interpreter, cellContainer, "GridCell");
        interpreter.renderer.pushContainer(cellContainer);

        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }
        return null;
    }
}
