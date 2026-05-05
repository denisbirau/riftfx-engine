package com.riftfx.error;

import com.riftfx.scanner.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
