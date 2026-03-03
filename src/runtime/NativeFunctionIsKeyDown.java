package runtime;

import java.util.List;

public class NativeFunctionIsKeyDown implements Callable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String key = ((String) arguments.getFirst()).toUpperCase();
        return GameState.keysPressed.contains(key);
    }
}
