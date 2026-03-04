package parsing;

import error.IErrorReporter;

import java.util.*;

public class Scanner {
    private final String sourceCode;
    private final IErrorReporter errorReporter;

    private int startIndex;
    private int currentIndex = 0;
    private int line = 1;

    private final List<Token> tokens = new ArrayList<>();

    private static final Map<Character, TokenType> SINGLE_CHAR_TOKENS = Map.ofEntries(
            Map.entry('+', TokenType.PLUS),
            Map.entry('-', TokenType.MINUS),
            Map.entry('*', TokenType.STAR),
            Map.entry('(', TokenType.LEFT_PARENTHESIS),
            Map.entry(')', TokenType.RIGHT_PARENTHESIS),
            Map.entry(';', TokenType.SEMICOLON),
            Map.entry(',', TokenType.COMMA),
            Map.entry('?', TokenType.QUESTION_MARK),
            Map.entry(':', TokenType.COLON),
            Map.entry('{', TokenType.LEFT_BRACE),
            Map.entry('}', TokenType.RIGHT_BRACE),
            Map.entry('.', TokenType.DOT),
            Map.entry('[', TokenType.LEFT_BRACKET),
            Map.entry(']', TokenType.RIGHT_BRACKET)
    );

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("let", TokenType.LET),
            Map.entry("print", TokenType.PRINT),
            Map.entry("and", TokenType.AND),
            Map.entry("or", TokenType.OR),
            Map.entry("true", TokenType.TRUE),
            Map.entry("false", TokenType.FALSE),
            Map.entry("null", TokenType.NULL),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("while", TokenType.WHILE),
            Map.entry("for", TokenType.FOR),
            Map.entry("break", TokenType.BREAK),
            Map.entry("def", TokenType.DEF),
            Map.entry("return", TokenType.RETURN),
            Map.entry("class", TokenType.CLASS),
            Map.entry("this", TokenType.THIS),
            Map.entry("extends", TokenType.EXTENDS),
            Map.entry("super", TokenType.SUPER)
    );

    public Scanner(String sourceCode, IErrorReporter errorReporter) {
        this.sourceCode = sourceCode;
        this.errorReporter = errorReporter;
    }

    public List<Token> scan() {
        while (!isAtEnd()) {
            startIndex = currentIndex;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return List.copyOf(tokens);
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;
            case '"' -> scanString();
            case '!' -> addToken(advanceIfNext('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
            case '=' -> addToken(advanceIfNext('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' -> addToken(advanceIfNext('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(advanceIfNext('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/' -> {
                if (advanceIfNext('/')) {
                    while (current() != '\n' && !isAtEnd()) advance();
                } else if (advanceIfNext('*')) {
                    scanMultilineComment();
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            default -> {
                if (SINGLE_CHAR_TOKENS.containsKey(c)) {
                    addToken(SINGLE_CHAR_TOKENS.get(c));
                } else if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifier();
                } else {
                    errorReporter.report("Unexpected character: " + c, line);
                }
            }
        }
    }

    private void scanString() {
        while (current() != '"' && !isAtEnd()) {
            if (current() == '\n') line ++;
            if (current() == '\\' && next() == '"') {
                advance(); // consume \
                advance(); // consume "
                continue;
            }
            advance();
        }
        if (isAtEnd()) {
            errorReporter.report("Unterminated string.", line);
            return;
        }
        advance(); // consume last "
        addToken(TokenType.STRING);
    }

    private void scanNumber() {
        while (isDigit(current())) advance();
        if (current() == '.' && isDigit(next())) {
            do advance();
            while (isDigit(current()));
        }
        addToken(TokenType.NUMBER);
    }

    private void scanIdentifier() {
        while (isAlNum(current())) advance();
        String lexeme = sourceCode.substring(startIndex, currentIndex);
        addToken(KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFIER));
    }

    private void scanMultilineComment() {
        while (!isAtEnd()) {
            if (current() == '\n') line ++;
            if (current() == '*' && next() == '/') {
                advance(); // consume *
                advance(); // consume /
                return;
            }
            advance();
        }
        errorReporter.report("Unterminated comment.", line);
    }

    // Helper methods
    private char advance() {
        return sourceCode.charAt(currentIndex++);
    }

    private boolean advanceIfNext(char expected) {
        if (isAtEnd()) return false;
        if (sourceCode.charAt(currentIndex) != expected) return false;
        currentIndex++;
        return true;
    }

    private char current() {
        if (isAtEnd()) return '\0';
        return sourceCode.charAt(currentIndex);
    }

    private char next() {
        if (currentIndex + 1 >= sourceCode.length()) return '\0';
        return sourceCode.charAt(currentIndex + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlNum(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private boolean isAtEnd() {
        return currentIndex >= sourceCode.length();
    }

    private void addToken(TokenType type) {
        String lexeme = sourceCode.substring(startIndex, currentIndex);
        tokens.add(new Token(type, lexeme, line));
    }
}
