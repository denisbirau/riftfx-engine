package com.riftfx.parser;

import com.riftfx.error.ParseError;
import com.riftfx.scanner.Token;
import com.riftfx.scanner.TokenType;

import java.util.List;

public class TokenStream {
    private final List<Token> tokens;
    private int currentIndex = 0;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token current() {
        return tokens.get(currentIndex);
    }

    public Token previous() {
        return tokens.get(currentIndex - 1);
    }

    public Token advance() {
        if (!isAtEnd()) {
            currentIndex++;
        }
        return previous();
    }

    public boolean check(TokenType tokenType) {
        if (isAtEnd()) {
            return false;
        }
        return current().type() == tokenType;
    }

    public boolean match(TokenType... tokenTypes) {
        for (var type : tokenTypes) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    public Token consume(TokenType tokenType, String errorMessage) {
        if (check(tokenType)) {
            return advance();
        }
        throw new ParseError(errorMessage, current());
    }

    public Token peek(int offset) {
        if (currentIndex + offset >= tokens.size()) {
            return tokens.getLast(); // Return the EOF
        }
        return tokens.get(currentIndex + offset);
    }

    public boolean isAtEnd() {
        return current().type() == TokenType.EOF;
    }
}
