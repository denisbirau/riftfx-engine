package com.riftfx.error;

import com.riftfx.scanner.Token;

public class ParseError extends RuntimeException {
    private final Token token;

    public ParseError(String errorMessage, Token token) {
        super(errorMessage);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
