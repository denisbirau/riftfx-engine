package compiler;

public enum TokenType {
    // Single character tokens.
    PLUS, MINUS, STAR, SLASH,
    LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
    SEMICOLON, COMMA,
    QUESTION_MARK, COLON,
    LEFT_BRACE, RIGHT_BRACE,
    DOT,

    // Single or double character tokens.
    EQUAL, EQUAL_EQUAL,
    NOT, NOT_EQUAL,
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,
    ARROW,

    // Literals
    STRING, NUMBER, IDENTIFIER,
    LEFT_BRACKET, RIGHT_BRACKET,

    // Keywords
    TRUE, FALSE, NULL,
    AND, OR,
    LET, PRINT,
    IF, ELSE,
    WHILE, FOR, BREAK,
    DEF, RETURN,
    CLASS, THIS, EXTENDS, SUPER,

    // End of file
    EOF
}
