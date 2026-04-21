package stdlib;

import interpreter.Callable;
import interpreter.Interpreter;
import interpreter.NativeObject;
import scanner.Token;

import java.util.List;

public class NativeArray implements NativeObject {
    public final List<Object> elements;

    public NativeArray(List<Object> elements) {
        this.elements = elements;
    }

    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "len" -> (double) elements.size();
            case "push" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    elements.add(arguments.getFirst());
                    return null;
                }
            };
            case "removeAt" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    Object index = arguments.getFirst();
                    if (index instanceof Double d) {
                        elements.remove(d.intValue());
                        return null;
                    }
                    throw new RuntimeException("Index must be a number.");
                }
            };
            default -> throw new RuntimeException("Undefined member on array: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Can not add new properties to an array.");
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
