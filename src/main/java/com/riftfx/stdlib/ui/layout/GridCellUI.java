package com.riftfx.stdlib.ui.layout;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import com.riftfx.stdlib.core.AbstractCallable;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.RendererUtils;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class GridCellUI extends AbstractCallable {
    public GridCellUI() {
        super(3, 4, "column", "row", "modifier", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        double row = InterpreterUtils.getArgument(arguments, 0, Double.class, 0.0);
        double col = InterpreterUtils.getArgument(arguments, 1, Double.class, 0.0);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 2, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 3, Callable.class, null);
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
        RendererUtils.applyModifier(cellContainer, "-fx-background-color: transparent;", modifierInstance);

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
