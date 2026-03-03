package runtime;

import java.awt.*;
import java.util.List;

public class NativeFunctionDrawText implements Callable {
    @Override
    public int arity() {
        return 4;
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String text = arguments.get(0).toString();
        int x = ((Double) arguments.get(1)).intValue();
        int y = ((Double) arguments.get(2)).intValue();
        String colorName = (String) arguments.get(3);

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
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString(text, x, y);
        });

        return null;
    }
}
