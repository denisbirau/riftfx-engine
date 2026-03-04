package compiler;

import ast.Expr;
import ast.Stmt;
import error.IErrorReporter;
import error.ParseError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final List<Token> tokens;
    private int currentIndex = 0;
    private final List<Stmt> statements = new ArrayList<>();
    private final IErrorReporter errorReporter;

    private enum Precedence {
        NONE,
        ASSIGNMENT, // =
        TERNARY,    // ?:
        OR,         // or
        AND,        // and
        EQUALITY,   // ==, !=
        COMPARISON, // <, >, <=, >=
        TERM,       // +, -
        FACTOR,     // *, /
        UNARY,      // !, -
        CALL,       // ., (), []
        PRIMARY
    }

    @FunctionalInterface
    interface PrefixParseFn {
        Expr parse();
    }

    @FunctionalInterface
    interface InfixParseFn {
        Expr parse(Expr leftExpression);
    }

    private record ParseRule(PrefixParseFn prefixParseFn, InfixParseFn infixParseFn, Precedence precedence) {}

    public Parser(List<Token> tokens, IErrorReporter errorReporter) {
        this.tokens = tokens;
        this.errorReporter = errorReporter;
    }

    public List<Stmt> parse() {
        while (!isAtEnd()) {
            statements.add(parseDeclaration());
        }
        return statements;
    }

    private Stmt parseDeclaration() {
        try {
            if (advanceIfNext(TokenType.LET)) return parseLetStatement();
            if (advanceIfNext(TokenType.DEF)) return parseDefStatement("function");
            if (advanceIfNext(TokenType.CLASS)) return parseClassStatement();
            return parseStatement();
        } catch (ParseError error) {
            skipToNextStatement();
            return null;
        }
    }

    private Stmt parseLetStatement() {
        Token identifier = expect(TokenType.IDENTIFIER, "Expect variable name after 'let' keyword.");
        Expr initializer = null;
        if (advanceIfNext(TokenType.EQUAL)) {
            initializer = parseExpression();
        }
        expect(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Let(identifier, initializer);
    }

    private Stmt.Def parseDefStatement(String type) {
        Token functionName = expect(TokenType.IDENTIFIER, "Expect " + type + " name.");
        expect(TokenType.LEFT_PARENTHESIS, "Expect '(' after "+ type +" name.");

        List<Token> parameters = new ArrayList<>();
        if (!checkCurrentType(TokenType.RIGHT_PARENTHESIS)) {
            do {
                parameters.add(expect(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (advanceIfNext(TokenType.COMMA));
        }
        expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after parameters.");
        expect(TokenType.LEFT_BRACE, "Expect '{' before " + type + " body.");

        Stmt.Block functionBody = (Stmt.Block) parseBlockStatement();
        return new Stmt.Def(functionName, parameters, functionBody.subStatements);
    }

    private Stmt parseClassStatement() {
        Token className = expect(TokenType.IDENTIFIER, "Expect class name after 'class'.");

        Expr.Lookup superclass = null;
        if (advanceIfNext(TokenType.EXTENDS)) {
            expect(TokenType.IDENTIFIER, "Expect superclass name after 'extends'.");
            superclass = new Expr.Lookup(getPreviousToken());
        }

        expect(TokenType.LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Def> methods = new ArrayList<>();
        while (!checkCurrentType(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            methods.add(parseDefStatement("method"));
        }
        expect(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(className, methods, superclass);
    }

    private Stmt parseStatement() {
        if (advanceIfNext(TokenType.PRINT)) return parsePrintStatement();
        if (advanceIfNext(TokenType.LEFT_BRACE)) return parseBlockStatement();
        if (advanceIfNext(TokenType.IF)) return parseIfStatement();
        if (advanceIfNext(TokenType.WHILE)) return parseWhileStatement();
        if (advanceIfNext(TokenType.FOR)) return parseForStatement();
        if (advanceIfNext(TokenType.BREAK)) return parseBreakStatement();
        if (advanceIfNext(TokenType.RETURN)) return parseReturnStatement();
        return parseExpressionStatement();
    }

    private Stmt parsePrintStatement() {
        Expr expression = parseExpression();
        expect(TokenType.SEMICOLON, "Expect ';' after print statement.");
        return new Stmt.Print(expression);
    }

    private Stmt parseBlockStatement() {
        List<Stmt> blockStatements = new ArrayList<>();
        while (!checkCurrentType(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            blockStatements.add(parseDeclaration());
        }
        expect(TokenType.RIGHT_BRACE, "Expect '}' after block statement.");
        return new Stmt.Block(blockStatements);
    }

    private Stmt parseIfStatement() {
        expect(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'if'.");
        Expr condition = parseExpression();
        expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after if condition.");

        Stmt thenStatement = parseStatement();
        Stmt elseStatement = null;
        if (advanceIfNext(TokenType.ELSE)) {
            elseStatement = parseStatement();
        }
        return new Stmt.If(condition, thenStatement, elseStatement);
    }

    private Stmt parseWhileStatement() {
        expect(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'while'.");
        Expr condition = parseExpression();
        expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after condition.");
        Stmt statement = parseStatement();
        return new Stmt.While(condition, statement);
    }

    private Stmt parseForStatement() {
        expect(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'for'.");

        // Initializer
        Stmt initializer;
        if (advanceIfNext(TokenType.LET)) {
            initializer = parseLetStatement();
        } else if (advanceIfNext(TokenType.SEMICOLON)) {
            initializer = null;
        } else {
            initializer = parseExpressionStatement();
        }

        // Looping condition
        Expr condition = null;
        if (!checkCurrentType(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }
        expect(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        // Incrementing
        Expr increment = null;
        if (!checkCurrentType(TokenType.RIGHT_PARENTHESIS)) {
            increment = parseExpression();
        }
        expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after for clauses.");

        // Body
        Stmt body = parseStatement();
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }
        body = new Stmt.While(Objects.requireNonNullElseGet(condition, () -> new Expr.Literal(true)), body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt parseBreakStatement() {
        Token keyword = getPreviousToken();
        expect(TokenType.SEMICOLON, "Expect ';' after 'break'.");
        return new Stmt.Break(keyword);
    }

    private Stmt parseReturnStatement() {
        Token keyword = getPreviousToken();
        Expr value = null;
        if (!checkCurrentType(TokenType.SEMICOLON)) {
            value = parseExpression();
        }
        expect(TokenType.SEMICOLON, "Expect ';' after return.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt parseExpressionStatement() {
        Expr expression = parseExpression();
        expect(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expression);
    }

    private Expr parseExpression() {
        return parsePrecedence(Precedence.ASSIGNMENT);
    }

    private Expr parsePrecedence(Precedence precedence) {
        advanceToNextToken();

        PrefixParseFn prefixParseFn = getRule(getPreviousToken().type).prefixParseFn;
        if (prefixParseFn == null) {
            throw error("Expect expression.", getPreviousToken());
        }

        Expr leftExpression = prefixParseFn.parse();
        while (precedence.ordinal() <= getRule(getCurrentToken().type).precedence.ordinal()) {
            advanceToNextToken();
            InfixParseFn infixParseFn = getRule(getPreviousToken().type).infixParseFn;
            leftExpression = infixParseFn.parse(leftExpression);
        }

        return leftExpression;
    }

    private ParseRule getRule(TokenType tokenType) {
        return switch (tokenType) {
            case TokenType.LEFT_PARENTHESIS      -> new ParseRule(this::parseGroupExpression, this::parseCallExpression, Precedence.CALL);
            case TokenType.LEFT_BRACKET          -> new ParseRule(this::parseArrayDefinitionExpression, this::parseSubscriptGetExpression, Precedence.CALL);
            case TokenType.DOT                   -> new ParseRule(null, this::parseDotExpression, Precedence.CALL);
            case TokenType.MINUS                 -> new ParseRule(this::parseUnaryExpression, this::parseBinaryExpression, Precedence.TERM);
            case TokenType.PLUS                  -> new ParseRule(null, this::parseBinaryExpression, Precedence.TERM);
            case TokenType.SLASH,
                 TokenType.STAR                  -> new ParseRule(null, this::parseBinaryExpression, Precedence.FACTOR);
            case TokenType.NOT                   -> new ParseRule(this::parseUnaryExpression, null, Precedence.NONE);
            case TokenType.EQUAL_EQUAL,
                 TokenType.NOT_EQUAL             -> new ParseRule(null, this::parseBinaryExpression, Precedence.EQUALITY);
            case TokenType.GREATER,
                 TokenType.GREATER_EQUAL,
                 TokenType.LESS,
                 TokenType.LESS_EQUAL            -> new ParseRule(null, this::parseBinaryExpression, Precedence.COMPARISON);
            case TokenType.AND                   -> new ParseRule(null, this::parseBinaryExpression, Precedence.AND);
            case TokenType.OR                    -> new ParseRule(null, this::parseBinaryExpression, Precedence.OR);
            case TokenType.EQUAL                 -> new ParseRule(null, this::parseAssignmentExpression, Precedence.ASSIGNMENT);
            case TokenType.QUESTION_MARK         -> new ParseRule(null, this::parseTernaryExpression, Precedence.TERNARY);
            case TokenType.NUMBER                -> new ParseRule(this::parseNumber, null, Precedence.NONE);
            case TokenType.STRING                -> new ParseRule(this::parseString, null, Precedence.NONE);
            case TokenType.TRUE,
                 TokenType.FALSE,
                 TokenType.NULL                  -> new ParseRule(this::parseLiteral, null, Precedence.NONE);
            case TokenType.IDENTIFIER            -> new ParseRule(this::parseIdentifier, null, Precedence.NONE);
            case TokenType.THIS                  -> new ParseRule(this::parseThisExpression, null, Precedence.NONE);
            case TokenType.SUPER                 -> new ParseRule(this::parseSuperExpression, null, Precedence.NONE);
            default                              -> new ParseRule(null, null, Precedence.NONE);
        };
    }

    private Expr parseGroupExpression() {
        Expr expression = parseExpression();
        expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression.");
        return new Expr.Group(expression);
    }

    private Expr parseCallExpression(Expr leftExpression) {
        Token leftParenthesis = getPreviousToken();
        List<Expr> arguments = new ArrayList<>();
        if (!checkCurrentType(TokenType.RIGHT_PARENTHESIS)) {
            do {
                arguments.add(parseExpression());
            } while (advanceIfNext(TokenType.COMMA));
        }
        expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after arguments.");
        return new Expr.Call(leftExpression, leftParenthesis, arguments);
    }

    private Expr parseArrayDefinitionExpression() {
        List<Expr> elements = new ArrayList<>();
        if (!checkCurrentType(TokenType.RIGHT_BRACKET)) {
            do {
                elements.add(parseExpression());
            } while (advanceIfNext(TokenType.COMMA));
        }
        expect(TokenType.RIGHT_BRACKET, "Expect ']' after array elements.");
        return new Expr.ArrayDefinition(elements);
    }

    private Expr parseSubscriptGetExpression(Expr leftExpression) {
        Token leftBracket = getPreviousToken();
        Expr index = parseExpression();
        expect(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
        return new Expr.SubscriptGet(leftExpression, leftBracket, index);
    }

    private Expr parseDotExpression(Expr leftExpression) {
        Token property = expect(TokenType.IDENTIFIER, "Expect property name after '.'.");
        return new Expr.Get(leftExpression, property);
    }

    private Expr parseUnaryExpression() {
        Token operator = getPreviousToken();
        Expr rightExpression = parsePrecedence(Precedence.UNARY);
        return new Expr.Unary(operator, rightExpression);
    }

    private Expr parseBinaryExpression(Expr leftExpression) {
        Token operator = getPreviousToken();
        ParseRule rule = getRule(operator.type);
        // We use + 1 because standard binary operators are left-associative
        Expr rightExpression = parsePrecedence(Precedence.values()[rule.precedence.ordinal() + 1]);
        return new Expr.Binary(leftExpression, operator, rightExpression);
    }

    private Expr parseAssignmentExpression(Expr leftExpression) {
        Token equalsToken = getPreviousToken();
        Expr rightExpression = parsePrecedence(Precedence.ASSIGNMENT);

        if (leftExpression instanceof Expr.Lookup lookup) {
            return new Expr.Assignment(lookup.identifier, rightExpression);
        } else if (leftExpression instanceof Expr.Get getExpression) {
            return new Expr.Set(getExpression.calleeExpression, getExpression.property, rightExpression);
        } else if (leftExpression instanceof Expr.SubscriptGet subscriptGet) {
            return new Expr.SubscriptSet(subscriptGet.array, subscriptGet.leftBracket, subscriptGet.index, rightExpression);
        }
        throw error("Invalid assignment target.", equalsToken);
    }

    private Expr parseTernaryExpression(Expr leftExpression) {
        Expr thenExpression = parseExpression();
        expect(TokenType.COLON, "Expect ':' in ternary operator.");
        // Ternary is right-associative, so we don't do + 1 on precedence
        Expr elseExpression = parsePrecedence(Precedence.TERNARY);
        return new Expr.Ternary(leftExpression, thenExpression, elseExpression);
    }

    private Expr parseNumber() {
        return new Expr.Literal(Double.valueOf(getPreviousToken().lexeme));
    }

    private Expr parseString() {
        String value = getPreviousToken().lexeme.substring(1, getPreviousToken().lexeme.length() - 1);
        return new Expr.Literal(value);
    }

    private Expr parseLiteral() {
        return switch (getPreviousToken().type) {
            case TokenType.TRUE -> new Expr.Literal(true);
            case TokenType.FALSE -> new Expr.Literal(false);
            case TokenType.NULL -> new Expr.Literal(null);
            default -> null; // Unreachable
        };
    }

    private Expr parseIdentifier() {
        return new Expr.Lookup(getPreviousToken());
    }

    private Expr parseThisExpression() {
        return new Expr.This(getPreviousToken());
    }

    private Expr parseSuperExpression() {
        Token keyword = getPreviousToken();
        expect(TokenType.DOT, "Expect '.' after 'super'.");
        Token method = expect(TokenType.IDENTIFIER, "Expect superclass method name.");
        return new Expr.Super(keyword, method);
    }

    private void skipToNextStatement() {
        advanceToNextToken();
        while (!isAtEnd()) {
            if (getPreviousToken().type == TokenType.SEMICOLON) return;
            if (checkCurrentType(TokenType.LET)) break;
            if (checkCurrentType(TokenType.PRINT)) break;
            if (checkCurrentType(TokenType.IF)) break;
            if (checkCurrentType(TokenType.WHILE)) break;
            if (checkCurrentType(TokenType.FOR)) break;
            if (checkCurrentType(TokenType.BREAK)) break;
            if (checkCurrentType(TokenType.DEF)) break;
            if (checkCurrentType(TokenType.RETURN)) break;
            advanceToNextToken();
        }
    }

    // Helper methods

    private Token getCurrentToken() {
        return tokens.get(currentIndex);
    }

    private Token getPreviousToken() {
        return tokens.get(currentIndex - 1);
    }

    /**
     * @return The previous token.*/
    private Token advanceToNextToken() {
        if (!isAtEnd()) currentIndex++;
        return getPreviousToken();
    }

    private boolean checkCurrentType(TokenType type) {
        if (isAtEnd()) return false;
        return getCurrentToken().type == type;
    }

    /**
     * Advances to next token if match*/
    private boolean advanceIfNext(TokenType... types) {
        for (TokenType type : types) {
            if (checkCurrentType(type)) {
                advanceToNextToken();
                return true;
            }
        }
        return false;
    }

    /**
     * Advance to next token if it matches the type parameter.
     * @return Previous token.
     * @throws ParseError if it doesn't match*/
    private Token expect(TokenType type, String errorMessage) {
        if (checkCurrentType(type)) return advanceToNextToken();
        throw error(errorMessage, getCurrentToken());
    }

    private boolean isAtEnd() {
        return getCurrentToken().type == TokenType.EOF;
    }

    private ParseError error(String message, Token keyword) {
        errorReporter.report(message, keyword.line);
        return new ParseError();
    }
}
