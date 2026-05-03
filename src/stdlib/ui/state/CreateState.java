package stdlib.ui.state;

import interpreter.Callable;
import interpreter.Interpreter;

import java.util.List;

public class CreateState implements Callable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("initialValue");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        return new State(arguments.getFirst());
    }
}
