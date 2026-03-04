package stdlib;

import engine.AssetManager;
import engine.GameState;
import runtime.Callable;
import runtime.Interpreter;

import java.awt.*;
import java.util.List;

public class NativeFunctionDrawSprite implements Callable {
    @Override
    public int arity() {
        return 5;
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String path = arguments.getFirst().toString();
        int x = ((Double) arguments.get(1)).intValue();
        int y = ((Double) arguments.get(2)).intValue();
        int w = ((Double) arguments.get(3)).intValue();
        int h = ((Double) arguments.get(4)).intValue();

        Image image = AssetManager.getImage(path);

        GameState.renderQueue.add((Graphics g) -> g.drawImage(image, x, y, w, h, null));

        return null;
    }
}
