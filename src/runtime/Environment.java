package runtime;

import compiler.Token;

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosingEnvironment;
    private final Map<String, Object> identifiers = new HashMap<>();

    Environment() {
        enclosingEnvironment = null;
    }

    Environment(Environment enclosingEnvironment) {
        this.enclosingEnvironment = enclosingEnvironment;
    }

    void define(String identifier, Object value) {
        identifiers.put(identifier, value);
    }

    Object getAt(String identifier, Integer distance) {
        return ancestor(distance).getValue(identifier);
    }

    Object updateAt(Token identifier, Object newValue, Integer distance) {
        return ancestor(distance).updateValue(identifier, newValue);
    }

    Object getValue(String identifier) {
        return identifiers.get(identifier);
    }

    Object updateValue(Token identifier, Object newValue) {
        identifiers.put(identifier.lexeme, newValue);
        return newValue;
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            if (environment != null) {
                environment = environment.enclosingEnvironment;
            }
        }
        return environment;
    }
}
