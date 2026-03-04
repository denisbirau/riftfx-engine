package stdlib;

import engine.GameState;
import runtime.Callable;
import runtime.Interpreter;

import java.awt.*;
import java.util.List;

public class NativeFunctionDrawRect implements Callable {
    @Override
    public int arity() {
        return 5;
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        int coordinateX = ((Double) arguments.get(0)).intValue();
        int coordinateY = ((Double) arguments.get(1)).intValue();
        int width = ((Double) arguments.get(2)).intValue();
        int height = ((Double) arguments.get(3)).intValue();
        String colorName = arguments.get(4).toString();

        Color color = Color.WHITE;
        color = switch (colorName) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            case "yellow" -> Color.YELLOW;
            default -> color;
        };

        Color finalColor = color;
        GameState.renderQueue.add((Graphics g) -> {
            g.setColor(finalColor);
            g.fillRect(coordinateX, coordinateY, width, height);
        });

        return null;
    }
}
