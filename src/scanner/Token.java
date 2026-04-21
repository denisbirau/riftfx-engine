package scanner;

public record Token(TokenType type, String lexeme, int line) { }
