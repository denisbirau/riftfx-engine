package runtime;

import java.util.List;

public interface Callable {
    int arity();
    Object call(List<Object> arguments, Interpreter interpreter);
}
