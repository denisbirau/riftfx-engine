package stdlib.core;

import interpreter.Callable;

import java.util.List;

public abstract class AbstractCallable implements Callable {
    private final int minArgs;
    private final int maxArgs;
    private final List<String> paramNames;

    public AbstractCallable(int minArgs, int maxArgs, String... paramNames) {
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.paramNames = List.of(paramNames);
    }

    @Override
    public int arity() {
        return maxArgs;
    }

    @Override
    public List<String> parameterNames() {
        return paramNames;
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount >= minArgs && argCount <= maxArgs;
    }
}
