package com.riftfx.scanner;

public record Token(TokenType type, String lexeme, int line) {
}
