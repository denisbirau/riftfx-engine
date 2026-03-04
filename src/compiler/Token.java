package compiler;

public class Token {
    public TokenType type;
    public String lexeme;
    public int line;

    Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + line;
    }
}
