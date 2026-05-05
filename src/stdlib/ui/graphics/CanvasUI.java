package stdlib.ui.graphics;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.canvas.Canvas;
import stdlib.ui.core.AbstractUIComponent;
import stdlib.ui.core.InterpreterUtils;

import java.util.List;

public class CanvasUI extends AbstractUIComponent {
    public CanvasUI() {
        super(3, 3, "width", "height", "onDraw");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        double width = InterpreterUtils.getArgument(arguments, 0, Double.class, 200.0);
        double height = InterpreterUtils.getArgument(arguments, 1, Double.class, 200.0);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);
        if (lambda == null) {
            throw new RuntimeException("Canvas requires a drawing lambda.");
        }

        Canvas canvas = new Canvas(width, height);
        NativeGraphicsContext nativeGraphicsContext = new NativeGraphicsContext(canvas.getGraphicsContext2D());
        lambda.call(List.of(nativeGraphicsContext), interpreter);

        register(interpreter, canvas);
        return null;
    }
}
