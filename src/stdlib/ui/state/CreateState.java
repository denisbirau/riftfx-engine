package stdlib.ui.state;

import interpreter.Interpreter;
import stdlib.core.AbstractCallable;

import java.util.List;

public class CreateState extends AbstractCallable {
    public CreateState() {
        super(1, 1, "initialValue");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        return new State(arguments.getFirst());
    }
}
