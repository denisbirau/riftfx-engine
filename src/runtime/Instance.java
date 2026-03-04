package runtime;

import compiler.Token;
import error.RuntimeError;

import java.util.HashMap;
import java.util.Map;

class Instance {
    private final Class myClass;
    private final Map<String, Object> fields = new HashMap<>();

    Instance(Class myClass) {
        this.myClass = myClass;
    }

    Object get(Token property) {
        if (fields.containsKey(property.lexeme)) {
            return fields.get(property.lexeme);
        }

        Function method = myClass.getMethod(property.lexeme);
        if (method != null) return method.bindInstance(this);

        throw new RuntimeError("Undefined property: '" + property.lexeme + "'.", property.line);
    }

    void set(Token property, Object newValue) {
        fields.put(property.lexeme, newValue);
    }

    @Override
    public String toString() {
        return myClass + " instance";
    }
}
