package stdlib;

import runtime.Callable;
import runtime.Interpreter;

import java.util.List;

public class NativeArrayTools {
    public static class Len implements Callable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            return (double) ((List<?>) arguments.getFirst()).size();
        }
    }

    public static class Push implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            ((List<Object>) arguments.getFirst()).add(arguments.get(1));
            return null;
        }
    }

    public static class RemoveAt implements Callable {
        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Object call(List<Object> arguments, Interpreter interpreter) {
            ((List<?>) arguments.getFirst()).remove(((Double) arguments.get(1)).intValue());
            return null;
        }
    }
}
