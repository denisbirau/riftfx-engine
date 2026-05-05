package com.riftfx.interpreter;

import com.riftfx.error.RuntimeError;
import com.riftfx.scanner.Token;

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

    void define(String identifierName, Object value) {
        identifiers.put(identifierName, value);
    }

    // Local Lookup
    Object getAt(String identifierName, int distance) {
        return ancestor(distance).identifiers.get(identifierName);
    }

    // Local Update
    void updateAt(String identifierName, int distance, Object newValue) {
        ancestor(distance).identifiers.put(identifierName, newValue);
    }

    // Global Lookup
    Object getGlobal(Token identifierToken) {
        if (identifiers.containsKey(identifierToken.lexeme())) {
            return identifiers.get(identifierToken.lexeme());
        }
        if (enclosingEnvironment != null) {
            return enclosingEnvironment.getGlobal(identifierToken);
        }
        throw new RuntimeError("'" + identifierToken + "' is undefined.", identifierToken);
    }

    // Global Update
    void updateGlobal(Token identifierToken, Object newValue) {
        if (identifiers.containsKey(identifierToken.lexeme())) {
            identifiers.put(identifierToken.lexeme(), newValue);
            return;
        }
        if (enclosingEnvironment != null) {
            enclosingEnvironment.updateGlobal(identifierToken, newValue);
            return;
        }
        throw new RuntimeError("'" + identifierToken.lexeme() + "' is undefined.", identifierToken);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (var i = 0; i < distance; i++) {
            if (environment != null) {
                environment = environment.enclosingEnvironment;
            }
        }
        return environment;
    }
}
