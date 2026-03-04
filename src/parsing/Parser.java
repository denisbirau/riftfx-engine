package parsing;

import ast.Expr;
import ast.Stmt;
import error.IErrorReporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Parser {
    private final List<Token> tokens;
    private int currentIndex = 0;
    private final List<Stmt> statements = new ArrayList<>();
    private final IErrorReporter errorReporter;

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
        return parseAssignmentExpression();
    }

    private Expr parseAssignmentExpression() {
        Expr leftExpression = parseTernaryExpression();
        if (advanceIfNext(TokenType.EQUAL)) {
            Token equalsToken = getPreviousToken();
            Expr rightExpression = parseAssignmentExpression();

            if (leftExpression instanceof Expr.Lookup lookup) {
                return new Expr.Assignment(lookup.identifier, rightExpression);
            } else if (leftExpression instanceof Expr.Get getExpression) {
                return new Expr.Set(getExpression.calleeExpression, getExpression.property, rightExpression);
            } else if (leftExpression instanceof Expr.SubscriptGet subscriptGet) {
                return new Expr.SubscriptSet(subscriptGet.array, subscriptGet.leftBracket, subscriptGet.index, rightExpression);
            }
            throw error("Invalid assignment target.", equalsToken);
        }
        return leftExpression;
    }

    private Expr parseTernaryExpression() {
        Expr condition = parseOrExpression();
        if (advanceIfNext(TokenType.QUESTION_MARK)) {
            Expr thenExpression = parseOrExpression();
            expect(TokenType.COLON, "Expect ':' in ternary operator.");
            Expr elseExpression = parseOrExpression();
            return new Expr.Ternary(condition, thenExpression, elseExpression);
        }
        return condition;
    }

    private Expr parseOrExpression() {
        return parseBinaryExpression(this::parseAndExpression, TokenType.OR);
    }

    private Expr parseAndExpression() {
        return parseBinaryExpression(this::parseEqualityExpression, TokenType.AND);
    }

    private Expr parseEqualityExpression() {
        return parseBinaryExpression(this::parseComparativeExpression, TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL);
    }

    private Expr parseComparativeExpression() {
        return parseBinaryExpression(this::parseAdditiveExpression,
                TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL);
    }

    private Expr parseAdditiveExpression() {
        return parseBinaryExpression(this::parseMultiplicativeExpression, TokenType.PLUS, TokenType.MINUS);
    }

    private Expr parseMultiplicativeExpression() {
        return parseBinaryExpression(this::parseUnaryExpression, TokenType.STAR, TokenType.SLASH);
    }

    private Expr parseBinaryExpression(Supplier<Expr> nextLevel, TokenType... operators) {
        Expr leftExpression = nextLevel.get();
        while (advanceIfNext(operators)) {
            Token operator = getPreviousToken();
            Expr rightExpression = nextLevel.get();
            leftExpression = new Expr.Binary(leftExpression, operator, rightExpression);
        }
        return leftExpression;
    }

    private Expr parseUnaryExpression() {
        if (advanceIfNext(TokenType.MINUS, TokenType.NOT)) {
            Token operator = getPreviousToken();
            Expr expression = parseUnaryExpression();
            return new Expr.Unary(operator, expression);
        }
        return parseCallExpression();
    }

    private Expr parseCallExpression() {
        Expr expression = parsePrimaryExpression();
        while (true) {
            if (advanceIfNext(TokenType.LEFT_PARENTHESIS)) {
                Token leftParenthesis = getPreviousToken();
                List<Expr> arguments = new ArrayList<>();
                if (!checkCurrentType(TokenType.RIGHT_PARENTHESIS)) {
                    do {
                        arguments.add(parseExpression());
                    } while (advanceIfNext(TokenType.COMMA));
                }
                expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after arguments.");
                expression = new Expr.Call(expression, leftParenthesis, arguments);
            } else if (advanceIfNext(TokenType.DOT)) {
                Token property = expect(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expression = new Expr.Get(expression, property);
            } else if (advanceIfNext(TokenType.LEFT_BRACKET)) {
                Token leftBracket = getPreviousToken();
                Expr index = parseExpression();
                expect(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
                expression = new Expr.SubscriptGet(expression, leftBracket, index);
            } else {
                break;
            }
        }
        return expression;
    }

    private Expr parsePrimaryExpression() {
        if (advanceIfNext(TokenType.TRUE)) return new Expr.Literal(true);
        if (advanceIfNext(TokenType.FALSE)) return new Expr.Literal(false);
        if (advanceIfNext(TokenType.NULL)) return new Expr.Literal(null);
        if (advanceIfNext(TokenType.NUMBER)) return new Expr.Literal(Double.valueOf(getPreviousToken().lexeme));
        if (advanceIfNext(TokenType.IDENTIFIER)) return new Expr.Lookup(getPreviousToken());
        if (advanceIfNext(TokenType.THIS)) return new Expr.This(getPreviousToken());
        if (advanceIfNext(TokenType.STRING)) {
            String value = getPreviousToken().lexeme.substring(1, getPreviousToken().lexeme.length() - 1);
            return new Expr.Literal(value);
        }
        if (advanceIfNext(TokenType.LEFT_PARENTHESIS)) {
            Expr expression = parseExpression();
            expect(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression.");
            return new Expr.Group(expression);
        }
        if (advanceIfNext(TokenType.SUPER)) {
            Token keyword = getPreviousToken();
            expect(TokenType.DOT, "Expect '.' after 'super'.");
            Token method = expect(TokenType.IDENTIFIER, "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }
        if (advanceIfNext(TokenType.LEFT_BRACKET)) {
            List<Expr> elements = new ArrayList<>();
            if (!checkCurrentType(TokenType.RIGHT_BRACKET)) {
                do {
                    elements.add(parseExpression());
                } while (advanceIfNext(TokenType.COMMA));
            }
            expect(TokenType.RIGHT_BRACKET, "Expect ']' after array elements.");
            return new Expr.ArrayDefinition(elements);
        }
        throw error("Expect expression.", getPreviousToken());
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
