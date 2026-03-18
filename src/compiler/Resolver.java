package compiler;

import ast.Expr;
import ast.Stmt;
import error.IErrorReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver {
    private final IErrorReporter errorReporter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    private boolean insideLoop = false;
    private boolean insideFunction = false;
    private boolean isConstructor = false;
    private boolean insideClass = false;
    private boolean hasSuperclass = false;

    public Resolver(IErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public void resolve(List<Stmt> statements) {
        beginNewScope();
        scopes.peek().put("len", true);
        scopes.peek().put("push", true);
        scopes.peek().put("removeAt", true);
        statements.forEach(this::resolve);
        endNewScope();
    }

    private void resolve(Stmt stmt) {
        switch (stmt) {
            case Stmt.Expression s -> resolve(s.expression);
            case Stmt.Let s        -> resolveLet(s);
            case Stmt.Print s      -> resolve(s.expression);
            case Stmt.Block s      -> resolveBlock(s);
            case Stmt.If s         -> resolveIf(s);
            case Stmt.While s      -> resolveWhile(s);
            case Stmt.Break s      -> resolveBreak(s);
            case Stmt.Def s        -> resolveDef(s);
            case Stmt.Return s     -> resolveReturn(s);
            case Stmt.Class s      -> resolveClass(s);
        }
    }

    private void resolve(Expr expr) {
        switch (expr) {
            case Expr.Literal _         -> {}
            case Expr.Unary e           -> resolve(e.expression);
            case Expr.Binary e          -> { resolve(e.leftExpression); resolve(e.rightExpression); }
            case Expr.Ternary e         -> { resolve(e.condition); resolve(e.thenExpression); resolve(e.elseExpression); }
            case Expr.Group e           -> resolve(e.expression);
            case Expr.Lookup e          -> resolveLookup(e);
            case Expr.Assignment e      -> { resolve(e.expression); resolveLocal(e, e.identifier); }
            case Expr.Call e            -> resolveCallExpr(e);
            case Expr.Get e             -> resolve(e.calleeExpression);
            case Expr.Set e             -> { resolve(e.calleeExpression); resolve(e.expression); }
            case Expr.This e            -> resolveThisExpr(e);
            case Expr.Super e           -> resolveSuperExpr(e);
            case Expr.ArrayDefinition e -> e.elements.forEach(this::resolve);
            case Expr.SubscriptGet e    -> { resolve(e.array); resolve(e.index); }
            case Expr.SubscriptSet e    -> { resolve(e.array); resolve(e.index); resolve(e.value); }
        }
    }

    private void resolveLocal(Expr expr, Token identifier) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(identifier.lexeme)) {
                int distance = scopes.size() - i - 1;
                switch (expr) {
                    case Expr.Lookup e     -> e.distance = distance;
                    case Expr.Assignment e -> e.distance = distance;
                    case Expr.This e       -> e.distance = distance;
                    case Expr.Super e      -> e.distance = distance;
                    default -> {}
                }
                return;
            }
        }
        errorReporter.report("'" + identifier.lexeme + "' was not declared.", identifier.line);
    }

    // Handlers
    private void resolveLet(Stmt.Let s) {
        declare(s.variableName); // We do not define it right away so we can't use its own name in the initializer
        if (s.initializer != null) resolve(s.initializer);
        define(s.variableName);
    }

    private void resolveBlock(Stmt.Block s) {
        beginNewScope();
        s.subStatements.forEach(this::resolve);
        endNewScope();
    }

    private void resolveIf(Stmt.If s) {
        resolve(s.condition);
        resolve(s.thenStatement);
        if (s.elseStatement != null) resolve(s.elseStatement);
    }

    private void resolveWhile(Stmt.While s) {
        resolve(s.condition);
        boolean aux = insideLoop;
        insideLoop = true;
        resolve(s.subStatement);
        insideLoop = aux;
    }

    private void resolveBreak(Stmt.Break s) {
        if (!insideLoop) errorReporter.report("Break statement outside loop.", s.keyword.line);
    }

    private void resolveDef(Stmt.Def s) {
        declare(s.functionName);
        define(s.functionName); // We define it right away for recursion

        beginNewScope();
        for (Token parameter : s.parameters) {
            declare(parameter);
            define(parameter);
        }
        boolean aux = insideFunction;
        insideFunction = true;
        for (Stmt statement : s.functionBody) resolve(statement);
        insideFunction = aux;
        endNewScope();
    }

    private void resolveReturn(Stmt.Return s) {
        if (!insideFunction) errorReporter.report("Return statement outside function.", s.keyword.line);
        if (s.expression != null) {
            if (isConstructor) errorReporter.report("Constructors can not return values.", s.keyword.line);
            resolve(s.expression);
        }
    }

    private void resolveClass(Stmt.Class s) {
        insideClass = true;
        declare(s.className);
        define(s.className);

        if (s.superclassExpression != null) {
            hasSuperclass = true;
            if (s.superclassExpression.identifier.lexeme.equals(s.className.lexeme)) {
                errorReporter.report(
                        "A class can not inherit from itself.",
                        s.superclassExpression.identifier.line
                );
            }
            resolve(s.superclassExpression);
            beginNewScope();
            defineSuperKeyword();
        }
        beginNewScope();
        defineThisKeyword();
        for (Stmt.Def method : s.methods) {
            isConstructor = method.functionName.lexeme.equals("constructor");
            resolve(method);
            isConstructor = false;
        }
        endNewScope();
        if (s.superclassExpression != null) {
            endNewScope();
        }

        insideClass = false;
        hasSuperclass = false;
    }

    private void resolveLookup(Expr.Lookup e) {
        if (lookup(e.identifier.lexeme) == Boolean.FALSE) {
            errorReporter.report("Variable name can not be used in its initializer.", e.identifier.line);
        }
        resolveLocal(e, e.identifier);
    }

    private void resolveCallExpr(Expr.Call e) {
        resolve(e.calleeExpression);
        e.arguments.forEach(this::resolve);
    }

    private void resolveThisExpr(Expr.This e) {
        if (!insideClass)
            errorReporter.report("'this' keyword outside class.", e.keyword.line);
        else
            resolveLocal(e, e.keyword);
    }

    private void resolveSuperExpr(Expr.Super e) {
        if (!insideClass)
            errorReporter.report("'super' outside a class.", e.keyword.line);
        else if (!hasSuperclass)
            errorReporter.report("No superclass defined for 'super'.", e.keyword.line);
        else
            resolveLocal(e, e.keyword);
    }

    // Helper methods
    private void beginNewScope() {
        scopes.push(new HashMap<>());
    }

    private void endNewScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.peek().containsKey(name.lexeme)) {
            errorReporter.report("'" + name.lexeme + "' was already declared in this scope.", name.line);
        }
        scopes.peek().put(name.lexeme, false);
    }

    private void define(Token name) {
        scopes.peek().put(name.lexeme, true);
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
