package interpreter;

import java.util.List;

public interface Callable {
    int arity();
    Object call(List<Object> arguments, Interpreter interpreter);

    default List<String> parameterNames() {
        return List.of();
    }

    // By default, functions have strict arity
    default boolean acceptsArity(int argCount) {
        return argCount == arity();
    }
}
