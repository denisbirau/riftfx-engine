package com.riftfx.stdlib.math;

import com.riftfx.interpreter.Interpreter;
import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.AbstractCallable;
import com.riftfx.stdlib.core.NativeObject;
import com.riftfx.stdlib.core.InterpreterUtils;

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

            case "floor" -> new AbstractCallable(1, 1, "value") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof Double d) {
                        return Math.floor(d);
                    }
                    throw new RuntimeException("floor expects a number.");
                }
            };
            case "round" -> new AbstractCallable(1, 2, "value", "decimals") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (!(arguments.getFirst() instanceof Double d)) {
                        throw new RuntimeException("Math.round expects a number.");
                    }

                    Double decimalsWrapper = InterpreterUtils.getArgument(arguments, 1, Double.class, 0.0);
                    double decimals = (decimalsWrapper != null) ? decimalsWrapper : 0.0;
                    double scale = Math.pow(10, decimals);

                    return Math.round(d * scale) / scale;
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
