package parser;

import ast.Expr;
import ast.Stmt;
import error.ErrorReporter;
import error.ParseError;
import scanner.Token;
import scanner.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final TokenStream stream;
    private final ExpressionParser expressionParser;

    public Parser(List<Token> tokens) {
        stream = new TokenStream(tokens);
        expressionParser = new ExpressionParser(this.stream, this);
    }

    // Parse Statements
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!stream.isAtEnd()) {
            try {
                statements.add(parseDeclaration());
            } catch (ParseError error) {
                ErrorReporter.report(error.getMessage(), error.getToken());
                skipToNextStatement();
            }
        }
        return statements;
    }

    Stmt parseDeclaration() {
        if (stream.match(TokenType.LET)) {
            return parseLetStatement();
        }
        if (stream.match(TokenType.DEF)) {
            return parseDefStatement("function");
        }
        if (stream.match(TokenType.CLASS)) {
            return parseClassStatement();
        }
        return parseStatement();
    }

    private Stmt parseLetStatement() {
        Token identifier = stream.consume(TokenType.IDENTIFIER, "Expect variable nameToken after 'let' keyword.");
        Expr initializer = null;
        if (stream.match(TokenType.EQUAL)) {
            initializer = expressionParser.parseExpression();
        }
        stream.consume(TokenType.SEMICOLON, "Expect ';' after statement.");
        return new Stmt.Let(identifier, initializer);
    }

    private Stmt.Def parseDefStatement(String type) {
        Token functionName = stream.consume(TokenType.IDENTIFIER, "Expect " + type + " nameToken after 'def' keyword.");
        stream.consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after "+ type +" nameToken.");

        List<Token> parameters = new ArrayList<>();
        if (!stream.check(TokenType.RIGHT_PARENTHESIS)) {
            do {
                parameters.add(stream.consume(TokenType.IDENTIFIER, "Expect parameter nameToken."));
            } while (stream.match(TokenType.COMMA));
        }
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after " + type + " parameters.");
        stream.consume(TokenType.LEFT_BRACE, "Expect '{' before " + type + " body.");

        Stmt.Block functionBody = (Stmt.Block) parseBlockStatement();
        return new Stmt.Def(functionName, parameters, functionBody.subStatements());
    }

    private Stmt parseClassStatement() {
        Token className = stream.consume(TokenType.IDENTIFIER, "Expect class nameToken after 'class' keyword.");

        Expr.Lookup superclassLookupExpression = null;
        if (stream.match(TokenType.EXTENDS)) {
            stream.consume(TokenType.IDENTIFIER, "Expect superclass nameToken after 'extends' keyword.");
            superclassLookupExpression = new Expr.Lookup(stream.previous());
        }

        stream.consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Def> methods = new ArrayList<>();
        while (!stream.check(TokenType.RIGHT_BRACE) && !stream.isAtEnd()) {
            methods.add(parseDefStatement("method"));
        }
        stream.consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(className, methods, superclassLookupExpression);
    }

    private Stmt parseStatement() {
        if (stream.match(TokenType.PRINT)) {
            return parsePrintStatement();
        }
        if (stream.match(TokenType.LEFT_BRACE)) {
            return parseBlockStatement();
        }
        if (stream.match(TokenType.IF)) {
            return parseIfStatement();
        }
        if (stream.match(TokenType.WHILE)) {
            return parseWhileStatement();
        }
        if (stream.match(TokenType.FOR)) {
            return parseForStatement();
        }
        if (stream.match(TokenType.BREAK)) {
            return parseBreakStatement();
        }
        if (stream.match(TokenType.RETURN)) {
            return parseReturnStatement();
        }

        return parseExpressionStatement();
    }

    private Stmt parsePrintStatement() {
        Expr expression = expressionParser.parseExpression();
        stream.consume(TokenType.SEMICOLON, "Expect ';' after print statement.");
        return new Stmt.Print(expression);
    }

    Stmt parseBlockStatement() {
        List<Stmt> subStatements = new ArrayList<>();
        while (!stream.check(TokenType.RIGHT_BRACE) && !stream.isAtEnd()) {
            subStatements.add(parseDeclaration());
        }
        stream.consume(TokenType.RIGHT_BRACE, "Expect '}' after block statement.");
        return new Stmt.Block(subStatements);
    }

    private Stmt parseIfStatement() {
        stream.consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'if' keyword.");
        Expr condition = expressionParser.parseExpression();
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression.");

        Stmt thenStatement = parseStatement();
        Stmt elseStatement = null;
        if (stream.match(TokenType.ELSE)) {
            elseStatement = parseStatement();
        }
        return new Stmt.If(condition, thenStatement, elseStatement);
    }

    private Stmt parseWhileStatement() {
        stream.consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'while' keyword.");
        Expr condition = expressionParser.parseExpression();
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression.");
        Stmt statement = parseStatement();
        return new Stmt.While(condition, statement);
    }

    private Stmt parseForStatement() {
        stream.consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'for' keyword.");

        // Initializer
        Stmt initializer;
        if (stream.match(TokenType.LET)) {
            initializer = parseLetStatement();
        } else if (stream.match(TokenType.SEMICOLON)) {
            initializer = null;
        } else {
            initializer = parseExpressionStatement();
        }

        // Looping condition
        Expr condition = null;
        if (!stream.check(TokenType.SEMICOLON)) {
            condition = expressionParser.parseExpression();
        }
        stream.consume(TokenType.SEMICOLON, "Expect ';' after expression.");

        // Incrementing
        Expr increment = null;
        if (!stream.check(TokenType.RIGHT_PARENTHESIS)) {
            increment = expressionParser.parseExpression();
        }
        stream.consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after expression.");

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
        Token keyword = stream.previous();
        stream.consume(TokenType.SEMICOLON, "Expect ';' after statement.");
        return new Stmt.Break(keyword);
    }

    private Stmt parseReturnStatement() {
        Token keyword = stream.previous();
        Expr value = null;
        if (!stream.check(TokenType.SEMICOLON)) {
            value = expressionParser.parseExpression();
        }
        stream.consume(TokenType.SEMICOLON, "Expect ';' after statement.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt parseExpressionStatement() {
        Expr expression = expressionParser.parseExpression();
        stream.consume(TokenType.SEMICOLON, "Expect ';' after statement.");
        return new Stmt.Expression(expression);
    }

    private void skipToNextStatement() {
        stream.advance();
        while (!stream.isAtEnd()) {
            if (stream.previous().type() == TokenType.SEMICOLON) return;
            if (stream.check(TokenType.LET)) break;
            if (stream.check(TokenType.PRINT)) break;
            if (stream.check(TokenType.IF)) break;
            if (stream.check(TokenType.WHILE)) break;
            if (stream.check(TokenType.FOR)) break;
            if (stream.check(TokenType.BREAK)) break;
            if (stream.check(TokenType.DEF)) break;
            if (stream.check(TokenType.RETURN)) break;
            stream.advance();
        }
    }
}
