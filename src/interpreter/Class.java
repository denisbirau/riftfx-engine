package interpreter;

import java.util.List;
import java.util.Map;

class Class implements Callable {
    private final String className;
    private final Map<String, Function> methods;
    private final Class superclass;

    Class(String className, Map<String, Function> methods, Class superclass) {
        this.className = className;
        this.methods = methods;
        this.superclass = superclass;
    }

    Function getMethod(String methodName) {
        if (methods.containsKey(methodName)) {
            return methods.get(methodName);
        }
        if (superclass != null) {
            return superclass.getMethod(methodName);
        }
        return null;
    }

    @Override
    public String toString() {
        return className;
    }

    @Override
    public int arity() {
        var constructor = getMethod("constructor");
        if (constructor == null)
            return 0;
        return constructor.arity();
    }

    @Override
    public List<String> parameterNames() {
        var constructor = getMethod("constructor");
        if (constructor == null) {
            return List.of();
        }
        return constructor.parameterNames();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        var instance = new Instance(this);
        var constructor = getMethod("constructor");
        if (constructor != null) {
            constructor.bindInstance(instance).call(arguments, interpreter);
        }
        return instance;
    }
}
