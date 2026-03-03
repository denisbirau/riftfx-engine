package runtime;

import java.util.List;

interface Callable {
    int arity();
    Object call(List<Object> arguments, Interpreter interpreter);
}
