package scanner;

import error.ErrorReporter;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String sourceCode;

    // Internal logic indexes
    private int startIndex = 0;
    private int currentIndex = 0;
    private int line = 1;

    private final List<Token> tokens = new ArrayList<>();

    private static final Map<Character, TokenType> SINGLE_CHARACTERS = Map.ofEntries(
            Map.entry('+', TokenType.PLUS),
            Map.entry('*', TokenType.STAR),
            Map.entry('%', TokenType.MODULO),
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

    private static final Map<Character, Pair<Character, Pair<TokenType, TokenType>>> SINGLE_OR_DOUBLE_CHARACTERS = Map.ofEntries(
            Map.entry('!', new Pair<>('=', new Pair<>(TokenType.NOT, TokenType.NOT_EQUAL))),
            Map.entry('=', new Pair<>('=', new Pair<>(TokenType.EQUAL, TokenType.EQUAL_EQUAL))),
            Map.entry('<', new Pair<>('=', new Pair<>(TokenType.LESS, TokenType.LESS_EQUAL))),
            Map.entry('>', new Pair<>('=', new Pair<>(TokenType.GREATER, TokenType.GREATER_EQUAL))),
            Map.entry('-', new Pair<>('>', new Pair<>(TokenType.MINUS, TokenType.ARROW)))
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

    public Scanner(String sourceCode) {
        this.sourceCode = sourceCode;
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
        if (getCurrentCharacter() == '\n') {
            line++;
            advanceCurrentCharacter(1);
        } else if (Character.isWhitespace(getCurrentCharacter())) {
            advanceCurrentCharacter(1);
        } else if (getCurrentCharacter() == '/') {
            scanSlash();
        } else if (getCurrentCharacter() == '"') {
            scanString();
        } else if (SINGLE_CHARACTERS.containsKey(getCurrentCharacter())) {
            scanSingleCharacter(SINGLE_CHARACTERS.get(getCurrentCharacter()));
        } else if (SINGLE_OR_DOUBLE_CHARACTERS.containsKey(getCurrentCharacter())) {
            var expectedChar = SINGLE_OR_DOUBLE_CHARACTERS.get(getCurrentCharacter()).getKey();
            var tokenTypes = SINGLE_OR_DOUBLE_CHARACTERS.get(getCurrentCharacter()).getValue();
            scanSingleOrDoubleCharacters(tokenTypes.getKey(), tokenTypes.getValue(), expectedChar);
        } else if (isDigit(getCurrentCharacter())) {
            scanNumber();
        } else if (isAlpha(getCurrentCharacter())) {
            scanIdentifier();
        } else {
            ErrorReporter.report("Unexpected character: " + getCurrentCharacter(), line);
        }
    }

    private void scanSingleCharacter(TokenType tokenType) {
        advanceCurrentCharacter(1);
        addToken(tokenType);
    }

    private void scanSlash() {
        if (nextCharacter() == '/') {
            scanSingleLineComment();
        } else if (nextCharacter() == '*') {
            scanMultilineComment();
        } else {
            scanSingleCharacter(TokenType.SLASH);
        }
    }

    private void scanSingleLineComment() {
        while (!isAtEnd() && getCurrentCharacter() != '\n') {
            advanceCurrentCharacter(1);
        }
    }

    private void scanMultilineComment() {
        advanceCurrentCharacter(2);
        while (!(getCurrentCharacter() == '*' && nextCharacter() == '/')) {
            if (isAtEnd()) {
                ErrorReporter.report("Unterminated comment.", line);
                return;
            } else if (getCurrentCharacter() == '\n') {
                line++;
            }
            advanceCurrentCharacter(1);
        }
        advanceCurrentCharacter(2); // To consume last * and /
    }

    private void scanSingleOrDoubleCharacters(TokenType singleChar, TokenType doubleChar, char expectedChar) {
        if (nextCharacter() == expectedChar) {
            advanceCurrentCharacter(2);
            addToken(doubleChar);
        } else {
            scanSingleCharacter(singleChar);
        }
    }

    private void scanString() {
        advanceCurrentCharacter(1);
        while (getCurrentCharacter() != '"') {
            if (isAtEnd() || getCurrentCharacter() == '\n') {
                ErrorReporter.report("Unterminated string.", line);
                return;
            } else if (isEscapeSequence()) {
                advanceCurrentCharacter(2);
            } else {
                advanceCurrentCharacter(1);
            }
        }
        advanceCurrentCharacter(1); // To consume last "
        addToken(TokenType.STRING);
    }

    private void scanNumber() {
        while (isDigit(getCurrentCharacter())) {
            advanceCurrentCharacter(1);
        }
        if (getCurrentCharacter() == '.' && isDigit(nextCharacter())) {
            do {
                advanceCurrentCharacter(1);
            } while (isDigit(getCurrentCharacter()));
        }
        addToken(TokenType.NUMBER);
    }

    private void scanIdentifier() {
        do {
            advanceCurrentCharacter(1);
        } while (isAlNum(getCurrentCharacter()));
        String lexeme = sourceCode.substring(startIndex, currentIndex);
        addToken(KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFIER));
    }

    private void advanceCurrentCharacter(int step) {
        if (!isAtEnd()) {
            currentIndex += step;
        }
    }

    private boolean isEscapeSequence() {
        if (currentIndex + 1 >= sourceCode.length()) {
            return false;
        }
        return getCurrentCharacter() == '\\' &&
                (nextCharacter() == '"' || nextCharacter() == '\\' ||
                        nextCharacter() == 'n' || nextCharacter() == 't' || nextCharacter() == 'b' ||
                        nextCharacter() == 'r' || nextCharacter() == 'f');
    }

    private char getCurrentCharacter() {
        if (isAtEnd()) {
            return '\0';
        }
        return sourceCode.charAt(currentIndex);
    }

    private char nextCharacter() {
        if (currentIndex + 1 >= sourceCode.length()) {
            return '\0';
        }
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
