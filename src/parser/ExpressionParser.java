package parser;

import ast.Expr;
import ast.Stmt;
import error.ParseError;
import scanner.Token;
import scanner.TokenType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser {
    private final TokenStream stream;
    private final Parser parser;
    private final Map<TokenType, ParseRule> rules = new EnumMap<>(TokenType.class);

    public ExpressionParser(TokenStream stream, Parser parser) {
        this.stream = stream;
        this.parser = parser;
        initializeRules();
    }

    public Expr parseExpression() {
        return parsePrecedence(Precedence.ASSIGNMENT);
    }

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
        PRIMARY;

        private static final Precedence[] VALUES = values();

        public Precedence next() {
            return VALUES[this.ordinal() + 1];
        }
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

    private void prefix(TokenType type, PrefixParseFn prefixFn) {
        rules.put(type, new ParseRule(prefixFn, null, Precedence.NONE));
    }

    private void infix(TokenType type, InfixParseFn infixFn, Precedence precedence) {
        rules.put(type, new ParseRule(null, infixFn, precedence));
    }

    private void mixed(TokenType type, PrefixParseFn prefixFn, InfixParseFn infixFn, Precedence precedence) {
        rules.put(type, new ParseRule(prefixFn, infixFn, precedence));
    }

    private void initializeRules() {
        // 1. Default all tokens to NONE
        for (TokenType type : TokenType.values()) {
            rules.put(type, new ParseRule(null, null, Precedence.NONE));
        }

        // 2. Register Prefix
        prefix(TokenType.NUMBER, this::parseNumber);
        prefix(TokenType.STRING, this::parseString);
        prefix(TokenType.TRUE, this::parseLiteral);
        prefix(TokenType.FALSE, this::parseLiteral);
        prefix(TokenType.NULL, this::parseLiteral);
        prefix(TokenType.IDENTIFIER, this::parseIdentifier);
        prefix(TokenType.THIS, this::parseThisExpression);
        prefix(TokenType.SUPER, this::parseSuperExpression);
        prefix(TokenType.NOT, this::parseUnaryExpression);
        prefix(TokenType.DEF, this::parseLambdaExpression);

        // 3. Register Infix
        infix(TokenType.PLUS, this::parseBinaryExpression, Precedence.TERM);
        infix(TokenType.STAR, this::parseBinaryExpression, Precedence.FACTOR);
        infix(TokenType.SLASH, this::parseBinaryExpression, Precedence.FACTOR);
        infix(TokenType.MODULO, this::parseBinaryExpression, Precedence.FACTOR);
        infix(TokenType.GREATER, this::parseBinaryExpression, Precedence.COMPARISON);
        infix(TokenType.GREATER_EQUAL, this::parseBinaryExpression, Precedence.COMPARISON);
        infix(TokenType.LESS, this::parseBinaryExpression, Precedence.COMPARISON);
        infix(TokenType.LESS_EQUAL, this::parseBinaryExpression, Precedence.COMPARISON);
        infix(TokenType.EQUAL_EQUAL, this::parseBinaryExpression, Precedence.EQUALITY);
        infix(TokenType.NOT_EQUAL, this::parseBinaryExpression, Precedence.EQUALITY);
        infix(TokenType.AND, this::parseBinaryExpression, Precedence.AND);
        infix(TokenType.OR, this::parseBinaryExpression, Precedence.OR);
        infix(TokenType.QUESTION_MARK, this::parseTernaryExpression, Precedence.TERNARY);
        infix(TokenType.EQUAL, this::parseAssignmentExpression, Precedence.ASSIGNMENT);
        infix(TokenType.DOT, this::parseDotExpression, Precedence.CALL);
        infix(TokenType.LEFT_BRACE, this::parseOmittedParenthesesCall, Precedence.CALL);

        // 4. Register Mixed
        mixed(TokenType.MINUS, this::parseUnaryExpression, this::parseBinaryExpression, Precedence.TERM);
        mixed(TokenType.LEFT_PARENTHESIS, this::parseGroupExpression, this::parseCallExpression, Precedence.CALL);
        mixed(TokenType.LEFT_BRACKET, this::parseArrayDefinitionExpression, this::parseSubscriptGetExpression, Precedence.CALL);
    }

    private Expr parsePrecedence(Precedence precedence) {
        stream.advance();

        PrefixParseFn prefixFn = rules.get(stream.previous().type()).prefixParseFn;
        if (prefixFn == null) {
            throw new ParseError("Expect subExpression.", stream.previous());
        }

        Expr leftExpression = prefixFn.parse();

        while (precedence.ordinal() <= rules.get(stream.current().type()).precedence().ordinal()) {
            stream.advance();
            InfixParseFn infixFn = rules.get(stream.previous().type()).infixParseFn;
            leftExpression = infixFn.parse(leftExpression);
        }

        return leftExpression;
    }

    private Expr parseNumber() {
        return new Expr.Literal(Double.valueOf(stream.previous().lexeme()));
    }

    private Expr parseString() {
        var lexeme = stream.previous().lexeme();
        var value = lexeme.substring(1, lexeme.length() - 1);
        return new Expr.Literal(value);
    }

    private Expr parseLiteral() {
        return switch (stream.previous().type()) {
            case TokenType.TRUE -> new Expr.Literal(true);
            case TokenType.FALSE -> new Expr.Literal(false);
            case TokenType.NULL -> new Expr.Literal(null);
            default -> null; // Unreachable
        };
    }

    private Expr parseIdentifier() {
        return new Expr.Lookup(stream.previous());
    }

    private Expr parseThisExpression() {
        return new Expr.This(stream.previous());
    }

    private Expr parseSuperExpression() {
        Token keyword = stream.previous();
        stream.consume(TokenType.DOT, "Expect '.' after 'super'.");
        Token memberIdentifier = stream.consume(TokenType.IDENTIFIER, "Expect superclass member nameToken.");
        return new Expr.Super(keyword, memberIdentifier);
    }

    private Expr parseUnaryExpression() {
        Token operator = stream.previous();
        Expr rightExpression = parsePrecedence(Precedence.UNARY);
        return new Expr.Unary(operator, rightExpression);
    }

    private Expr parseGroupExpression() {
        Expr innerExpression = parseExpression();
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after subExpression.");
        return new Expr.Group(innerExpression);
    }

    private Expr parseArrayDefinitionExpression() {
        List<Expr> elements = new ArrayList<>();
        if (!stream.check(TokenType.RIGHT_BRACKET)) {
            do {
                elements.add(parseExpression());
            } while (stream.match(TokenType.COMMA));
        }
        stream.consume(TokenType.RIGHT_BRACKET, "Expect ']' after sequenceExpression elements.");
        return new Expr.ArrayDefinition(elements);
    }

    private Expr parseLambdaExpression() {
        stream.consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'def' keyword.");
        List<Token> parameters = new ArrayList<>();
        if (!stream.check(TokenType.RIGHT_PARENTHESIS)) {
            do {
                parameters.add(stream.consume(TokenType.IDENTIFIER, "Expect parameter nameToken."));
            } while (stream.match(TokenType.COMMA));
        }
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after parameters.");

        stream.consume(TokenType.LEFT_BRACE, "Expect '{' before lambda body.");
        Stmt.Block block = (Stmt.Block) parser.parseBlockStatement();

        return new Expr.Lambda(parameters, block.subStatements());
    }

    private Expr parseBinaryExpression(Expr leftExpression) {
        Token operator = stream.previous();
        ParseRule rule = rules.get(operator.type());
        // We use + 1 because standard binary operators are left-associative
        Expr rightExpression = parsePrecedence(rule.precedence.next());
        return new Expr.Binary(leftExpression, operator, rightExpression);
    }

    private Expr parseTernaryExpression(Expr leftExpression) {
        Expr thenExpression = parseExpression();
        stream.consume(TokenType.COLON, "Expect ':' in ternary operator.");
        // Ternary is right-associative, so we don't do + 1 on precedence
        Expr elseExpression = parsePrecedence(Precedence.TERNARY);
        return new Expr.Ternary(leftExpression, thenExpression, elseExpression);
    }

    private Expr parseAssignmentExpression(Expr leftExpression) {
        Expr expressionToAssign = parsePrecedence(Precedence.ASSIGNMENT);

        if (leftExpression instanceof Expr.Lookup lookup) {
            return new Expr.Assignment(lookup.identifierToken(), expressionToAssign);
        } else if (leftExpression instanceof Expr.GetMember(Expr objectExpression, Token memberIdentifier)) {
            return new Expr.SetMember(objectExpression, memberIdentifier, expressionToAssign);
        } else if (leftExpression instanceof Expr.SubscriptGet(Expr array, Token leftBracket, Expr indexExpression)) {
            return new Expr.SubscriptSet(array, leftBracket, indexExpression, expressionToAssign);
        }
        throw new ParseError("Invalid assignment target.", stream.previous());
    }

    private Expr parseDotExpression(Expr leftExpression) {
        Token memberIdentifier = stream.consume(TokenType.IDENTIFIER, "Expect member nameToken after '.'.");
        return new Expr.GetMember(leftExpression, memberIdentifier);
    }

    private Expr parseOmittedParenthesesCall(Expr leftExpression) {
        Token leftParenthesis = stream.previous();
        List<Expr.Argument> arguments = new ArrayList<>();
        arguments.add(new Expr.Argument(null, parseTrailingLambdaBlock()));
        return new Expr.Call(leftExpression, leftParenthesis, arguments);
    }

    private Expr parseSubscriptGetExpression(Expr leftExpression) {
        Token leftBracket = stream.previous();
        Expr indexExpression = parseExpression();
        stream.consume(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
        return new Expr.SubscriptGet(leftExpression, leftBracket, indexExpression);
    }

    private Expr parseCallExpression(Expr leftExpression) {
        Token leftParenthesis = stream.previous();
        List<Expr.Argument> arguments = new ArrayList<>();
        if (!stream.check(TokenType.RIGHT_PARENTHESIS)) {
            do {
                Token name = null;
                if (stream.check(TokenType.IDENTIFIER) && stream.peek(1).type() == TokenType.EQUAL) {
                    name = stream.advance();
                    stream.advance(); // Consume '='
                }
                arguments.add(new Expr.Argument(name, parseExpression()));
            } while (stream.match(TokenType.COMMA));
        }
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after arguments.");
        if (stream.match(TokenType.LEFT_BRACE)) {
            arguments.add(new Expr.Argument(null, parseTrailingLambdaBlock()));
        }
        return new Expr.Call(leftExpression, leftParenthesis, arguments);
    }

    private Expr parseTrailingLambdaBlock() {
        List<Token> parameters = new ArrayList<>();
        boolean hasArrow = false;

        // Check to see if it has an arrow
        int lookahead = 0;
        while (true) {
            TokenType tokenType = stream.peek(lookahead).type();
            if (tokenType == TokenType.ARROW) {
                hasArrow = true;
                break;
            } else if (tokenType != TokenType.IDENTIFIER && tokenType != TokenType.COMMA) {
                break;
            }
            lookahead++;
        }

        // If it has an arrow parse parameters
        if (hasArrow) {
            if (!stream.check(TokenType.ARROW)) {
                do {
                    parameters.add(stream.consume(TokenType.IDENTIFIER, "Expect parameter nameToken."));
                } while (stream.match(TokenType.COMMA));
            }
            stream.consume(TokenType.ARROW, "Expect '->' after lambda parameters.");
        }

        // Parse lambda body
        List<Stmt> body = new ArrayList<>();
        while (!stream.check(TokenType.RIGHT_BRACE) && !stream.isAtEnd()) {
            body.add(parser.parseDeclaration());
        }
        stream.consume(TokenType.RIGHT_BRACE, "Expect '}' after lambda body.");

        return new Expr.Lambda(parameters, body);
    }
}
