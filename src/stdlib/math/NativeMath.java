package stdlib.math;

import interpreter.Interpreter;
import scanner.Token;
import stdlib.core.AbstractCallable;
import stdlib.core.NativeObject;

import java.util.List;

public class NativeMath implements NativeObject {
    @Override
    public Object getMember(Token member) {
        // Constants
        return switch (member.lexeme()) {
            case "PI" -> Math.PI;
            case "E" -> Math.E;

            // Methods
            case "sqrt" -> new AbstractCallable(1, 1, "value") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof Double d) {
                        return Math.sqrt(d);
                    }
                    throw new RuntimeException("sqrt expects a number.");
                }
            };
            case "random" -> new AbstractCallable(0, 0) {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    return Math.random();
                }
            };

            case "floor" ->  new AbstractCallable(1, 1, "value") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof Double d) {
                        return Math.floor(d);
                    }
                    throw new RuntimeException("floor expects a number.");
                }
            };
            default -> throw new RuntimeException("Undefined Math property: '" + member.lexeme() + "'.");
        };

    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Cannot mutate the standard Math library.");
    }
}
