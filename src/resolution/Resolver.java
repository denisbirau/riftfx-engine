package resolution;

import ast.Expr;
import ast.Stmt;
import error.ErrorReporter;
import scanner.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver {
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final ErrorReporter errorReporter;

    private boolean insideLoop = false;
    private boolean insideFunction = false;
    private boolean isConstructor = false;
    private boolean insideClass = false;
    private boolean hasSuperclass = false;

    public Resolver(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        beginNewScope();
    }

    public void resolve(List<Stmt> statements) {
        statements.forEach(this::resolve);
    }

    private void resolve(Stmt stmt) {
        switch (stmt) {
            case Stmt.Expression s -> resolveExpressionStatement(s);
            case Stmt.Let s        -> resolveLetStatement(s);
            case Stmt.Print s      -> resolvePrintStatement(s);
            case Stmt.Block s      -> resolveBlockStatement(s);
            case Stmt.If s         -> resolveIfStatement(s);
            case Stmt.While s      -> resolveWhileStatement(s);
            case Stmt.Break s      -> resolveBreakStatement(s);
            case Stmt.Def s        -> resolveDefStatement(s);
            case Stmt.Return s     -> resolveReturnStatement(s);
            case Stmt.Class s      -> resolveClassStatement(s);
        }
    }

    private void resolve(Expr expr) {
        switch (expr) {
            case Expr.Literal e         -> resolveLiteralExpression(e);
            case Expr.Unary e           -> resolveUnaryExpression(e);
            case Expr.Binary e          -> resolveBinaryExpression(e);
            case Expr.Ternary e         -> resolveTernaryExpression(e);
            case Expr.Group e           -> resolveGroupExpression(e);
            case Expr.Lookup e          -> resolveLookupExpression(e);
            case Expr.Assignment e      -> resolveAssignmentExpression(e);
            case Expr.Call e            -> resolveCallExpression(e);
            case Expr.GetMember e       -> resolveGetMemberExpression(e);
            case Expr.SetMember e       -> resolveSetMemberExpression(e);
            case Expr.This e            -> resolveThisExpression(e);
            case Expr.Super e           -> resolveSuperExpression(e);
            case Expr.ArrayDefinition e -> resolveArrayDefinitionExpression(e);
            case Expr.SubscriptGet e    -> resolveSubscriptGetExpression(e);
            case Expr.SubscriptSet e    -> resolveSubscriptSetExpression(e);
            case Expr.Lambda e          -> resolveLambdaExpression(e);
        }
    }

    private void resolveLocal(Expr expr, Token identifier) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(identifier.lexeme())) {
                int distance = scopes.size() - i - 1;
                switch (expr) {
                    case Expr.Lookup e     -> e.resolution().distance = distance;
                    case Expr.Assignment e -> e.resolution().distance = distance;
                    case Expr.This e       -> e.resolution().distance = distance;
                    case Expr.Super e      -> e.resolution().distance = distance;
                    default -> {}
                }
                return;
            }
        }
    }

    // Statement Handlers
    private void resolveExpressionStatement(Stmt.Expression stmt) {
        resolve(stmt.expression());
    }

    private void resolveLetStatement(Stmt.Let stmt) {
        declare(stmt.variableName()); // We do not define it right away so we can't use its own nameToken in the initializer
        if (stmt.initializer() != null) {
            resolve(stmt.initializer());
        }
        define(stmt.variableName());
    }

    private void resolvePrintStatement(Stmt.Print stmt) {
        resolve(stmt.expression());
    }

    private void resolveBlockStatement(Stmt.Block stmt) {
        beginNewScope();
        stmt.subStatements().forEach(this::resolve);
        endNewScope();
    }

    private void resolveIfStatement(Stmt.If stmt) {
        resolve(stmt.condition());
        resolve(stmt.thenStatement());
        if (stmt.elseStatement() != null) {
            resolve(stmt.elseStatement());
        }
    }

    private void resolveWhileStatement(Stmt.While stmt) {
        resolve(stmt.condition());
        boolean aux = insideLoop;
        insideLoop = true;
        resolve(stmt.subStatement());
        insideLoop = aux;
    }

    private void resolveBreakStatement(Stmt.Break stmt) {
        if (!insideLoop) {
            errorReporter.report("Break statement outside loop.", stmt.keyword().line());
        }
    }

    private void resolveDefStatement(Stmt.Def stmt) {
        declare(stmt.name());
        define(stmt.name()); // We define it right away for recursion

        beginNewScope();
        for (Token parameter : stmt.parameters()) {
            declare(parameter);
            define(parameter);
        }
        boolean aux = insideFunction;
        insideFunction = true;
        stmt.body().forEach(this::resolve);
        insideFunction = aux;
        endNewScope();
    }

    private void resolveReturnStatement(Stmt.Return stmt) {
        if (!insideFunction) {
            errorReporter.report("Return statement outside function.", stmt.keyword().line());
        }
        else if (stmt.expression() != null && isConstructor) {
            errorReporter.report("Constructors can not return values.", stmt.keyword().line());
        }
        else if (stmt.expression() != null) {
            resolve(stmt.expression());
        }
    }

    private void resolveClassStatement(Stmt.Class stmt) {
        insideClass = true;
        declare(stmt.className());
        define(stmt.className());

        if (stmt.superclassLookupExpression() != null) {
            hasSuperclass = true;
            if (stmt.superclassLookupExpression().identifierToken().lexeme().equals(stmt.className().lexeme())) {
                errorReporter.report(
                        "A class can not inherit from itself.",
                        stmt.superclassLookupExpression().identifierToken().line()
                );
            }
            resolve(stmt.superclassLookupExpression());
            beginNewScope();
            defineSuperKeyword();
        }
        beginNewScope();
        defineThisKeyword();
        for (Stmt.Def method : stmt.methods()) {
            isConstructor = method.name().lexeme().equals("constructor");
            resolve(method);
            isConstructor = false;
        }
        endNewScope();
        if (stmt.superclassLookupExpression() != null) {
            endNewScope();
        }

        insideClass = false;
        hasSuperclass = false;
    }

    // Expression Handlers
    private void resolveLiteralExpression(Expr.Literal ignoredExpr) {
    }

    private void resolveUnaryExpression(Expr.Unary expr) {
        resolve(expr.expression());
    }

    private void resolveBinaryExpression(Expr.Binary expr) {
        resolve(expr.leftExpression());
        resolve(expr.rightExpression());
    }

    private void resolveTernaryExpression(Expr.Ternary expr) {
        resolve(expr.condition());
        resolve(expr.thenExpression());
        resolve(expr.elseExpression());
    }

    private void resolveGroupExpression(Expr.Group expr) {
        resolve(expr.innerExpression());
    }

    private void resolveLookupExpression(Expr.Lookup expr) {
        if (lookup(expr.identifierToken().lexeme()) == Boolean.FALSE) {
            errorReporter.report("Variable nameToken can not be used in its initializer.", expr.identifierToken().line());
        }
        resolveLocal(expr, expr.identifierToken());
    }

    private void resolveAssignmentExpression(Expr.Assignment expr) {
        resolve(expr.expressionToAssign());
        resolveLocal(expr, expr.identifierToken());
    }

    private void resolveCallExpression(Expr.Call expr) {
        resolve(expr.calleeExpression());
        for (Expr.Argument argument : expr.arguments()) {
            resolve(argument.value());
        }
    }

    private void resolveGetMemberExpression(Expr.GetMember expr) {
        resolve(expr.objectExpression());
    }

    private void resolveSetMemberExpression(Expr.SetMember expr) {
        resolve(expr.objectExpression());
        resolve(expr.expressionToAssign());
    }

    private void resolveThisExpression(Expr.This expr) {
        if (!insideClass) {
            errorReporter.report("'this' keyword outside class.", expr.keyword().line());
        } else {
            resolveLocal(expr, expr.keyword());
        }
    }

    private void resolveSuperExpression(Expr.Super expr) {
        if (!insideClass) {
            errorReporter.report("'super' outside a class.", expr.keyword().line());
        } else if (!hasSuperclass) {
            errorReporter.report("No superclass defined for 'super'.", expr.keyword().line());
        } else {
            resolveLocal(expr, expr.keyword());
        }
    }

    private void resolveArrayDefinitionExpression(Expr.ArrayDefinition expr) {
        expr.elements().forEach(this::resolve);
    }

    private void resolveSubscriptGetExpression(Expr.SubscriptGet expr) {
        resolve(expr.array());
        resolve(expr.indexExpression());
    }

    private void resolveSubscriptSetExpression(Expr.SubscriptSet expr) {
        resolve(expr.array());
        resolve(expr.indexExpression());
        resolve(expr.expressionToAssign());
    }

    private void resolveLambdaExpression(Expr.Lambda expr) {
        beginNewScope();
        for (Token parameter : expr.parameters()) {
            declare(parameter);
            define(parameter);
        }
        boolean aux = insideFunction;
        insideFunction = true;
        for (Stmt stmt : expr.lambdaBody()) {
            resolve(stmt);
        }
        insideFunction = aux;
        endNewScope();
    }

    // Helper methods
    private void beginNewScope() {
        scopes.push(new HashMap<>());
    }

    private void endNewScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.peek().containsKey(name.lexeme())) {
            errorReporter.report("'" + name.lexeme() + "' was already declared in this scope.", name.line());
        }
        scopes.peek().put(name.lexeme(), false);
    }

    private void define(Token name) {
        scopes.peek().put(name.lexeme(), true);
    }

    private void defineSuperKeyword() {
        scopes.peek().put("super", true);
    }

    private void defineThisKeyword() {
        scopes.peek().put("this", true);
    }

    private Boolean lookup(String identifier) {
        return scopes.peek().get(identifier);
    }
}
