package interpreter;

import java.util.List;

public interface Callable {
    int arity();
    Object call(List<Object> arguments, Interpreter interpreter);
    default boolean acceptsArity(int argCount) {
        return argCount == arity();
    }
}
