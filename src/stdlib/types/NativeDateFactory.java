package stdlib.types;

import interpreter.Interpreter;
import scanner.Token;
import stdlib.core.AbstractCallable;
import stdlib.core.NativeObject;

import java.time.LocalDate;
import java.util.List;

public class NativeDateFactory implements NativeObject {
    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "now" -> new AbstractCallable(0, 0) {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    return new NativeDate(LocalDate.now());
                }
            };
            case "parse" -> new AbstractCallable(1, 1, "string") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    try {
                        return new NativeDate(LocalDate.parse(arguments.getFirst().toString()));
                    } catch (Exception _) {
                        throw new RuntimeException("Invalid date format. Use YYYY-MM-DD.");
                    }
                }
            };
            default -> throw new RuntimeException("Undefined member: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Cannot modify the global Date object.");
    }
}
