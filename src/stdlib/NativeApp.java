package stdlib;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.application.Platform;
import scanner.Token;

import java.util.List;

public class NativeApp implements NativeObject {
    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "exit" -> new Callable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public boolean acceptsArity(int argCount) {
                    return argCount == 0 || argCount == 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    int exitCode = 0;
                    if (!arguments.isEmpty() && arguments.getFirst() instanceof Double d) {
                        exitCode = d.intValue();
                    }
                    Platform.exit();
                    System.exit(exitCode);
                    return null;
                }
            };
            default -> throw new RuntimeException("Undefined member: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Cannot modify the global App object.");
    }
}
