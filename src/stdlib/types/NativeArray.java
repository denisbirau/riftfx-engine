package stdlib.types;

import interpreter.Interpreter;
import scanner.Token;
import stdlib.core.AbstractCallable;
import stdlib.core.NativeObject;

import java.util.List;

public record NativeArray(List<Object> elements) implements NativeObject {
    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "len" -> (double) elements.size();
            case "push" -> new AbstractCallable(1, 1, "item") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    elements.add(arguments.getFirst());
                    return null;
                }
            };
            case "removeAt" -> new AbstractCallable(1, 1, "index") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof Double d) {
                        int i = d.intValue();
                        if (i < 0 || i >= elements.size()) {
                            throw new RuntimeException("Array index out of bounds.");
                        }
                        elements.remove(i);
                        return null;
                    }
                    throw new RuntimeException("Index must be a number.");
                }
            };
            case "indexOf" -> new AbstractCallable(1, 1, "item") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    return (double) elements.indexOf(arguments.getFirst());
                }
            };
            default -> throw new RuntimeException("Undefined member on sequenceExpression: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Can not add new properties to an sequenceExpression.");
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
