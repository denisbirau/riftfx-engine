package runtime;

import ast.Expr;
import ast.Stmt;
import error.IErrorReporter;
import compiler.Token;
import compiler.TokenType;
import error.RuntimeError;
import stdlib.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter {
    public Environment globalEnvironment = new Environment();
    Environment currentEnvironment = globalEnvironment;

    private final List<Stmt> statements;
    private final IErrorReporter errorReporter;

    public Interpreter(List<Stmt> statements, IErrorReporter errorReporter) {
        this.statements = statements;
        this.errorReporter = errorReporter;

        // Native functions
        globalEnvironment.define("len", new NativeArrayTools.Len());
        globalEnvironment.define("push", new NativeArrayTools.Push());
        globalEnvironment.define("removeAt", new NativeArrayTools.RemoveAt());
        globalEnvironment.define("isKeyDown", new NativeFunctionIsKeyDown());
        globalEnvironment.define("drawRect", new NativeFunctionDrawRect());
        globalEnvironment.define("drawText", new NativeFunctionDrawText());
        globalEnvironment.define("drawSprite", new NativeFunctionDrawSprite());
        globalEnvironment.define("playSound", new NativeFunctionPlaySound());
    }

    public void callScriptFunction(String identifier) {
        Object function = currentEnvironment.getValue(identifier);
        if (function instanceof Callable callable) {
            callable.call(List.of(), this);
        }
    }

    public void callScriptFunction(String identifier, List<Object> arguments) {
        Object function = currentEnvironment.getValue(identifier);
        if (function instanceof Callable callable) {
            callable.call(arguments, this);
        }
    }

    public void interpret() {
        for (Stmt statement : statements) {
            try {
                execute(statement);
            } catch (RuntimeError error) {
                errorReporter.report(error.message, error.line);
                break;
            }
        }
    }

    void execute(Stmt stmt) {
        switch (stmt) {
            case Stmt.Expression s -> evaluate(s.expression);
            case Stmt.Let s        -> executeLet(s);
            case Stmt.Print s      -> System.out.println(stringify(evaluate(s.expression)));
            case Stmt.Block s      -> executeBlock(s);
            case Stmt.If s         -> executeIf(s);
            case Stmt.While s      -> executeWhile(s);
            case Stmt.Break s      -> throw new Break(s.keyword.line);
            case Stmt.Def s        -> executeDef(s);
            case Stmt.Return s     -> executeReturn(s);
            case Stmt.Class s      -> executeClass(s);
        }
    }

    private Object evaluate(Expr expr) {
        return switch (expr) {
            case Expr.Literal e         -> e.value;
            case Expr.Unary e           -> evaluateUnary(e);
            case Expr.Binary e          -> evaluateBinary(e);
            case Expr.Ternary e         -> isTrue(evaluate(e.condition)) ? evaluate(e.thenExpression) : evaluate(e.elseExpression);
            case Expr.Group e           -> evaluate(e.expression);
            case Expr.Lookup e          -> {
                if (e.distance != null) {
                    yield currentEnvironment.getAt(e.identifier.lexeme, e.distance);
                } else {
                    yield globalEnvironment.getValue(e.identifier.lexeme);
                }
            }
            case Expr.Assignment e      -> {
                Object value = evaluate(e.expression);
                if (e.distance != null) {
                    currentEnvironment.updateAt(e.identifier, value, e.distance);
                } else {
                    globalEnvironment.updateValue(e.identifier, value);
                }
                yield value;
            }
            case Expr.Call e            -> evaluateCall(e);
            case Expr.Get e             -> evaluateGet(e);
            case Expr.Set e             -> evaluateSet(e);
            case Expr.This e            -> currentEnvironment.getAt("this", e.distance);
            case Expr.Super e           -> evaluateSuper(e);
            case Expr.ArrayDefinition e -> evaluateArrayDefinition(e);
            case Expr.SubscriptGet e    -> evaluateSubscriptGet(e);
            case Expr.SubscriptSet e    -> evaluateSubscriptSet(e);
        };
    }

    // Handlers
    private void executeLet(Stmt.Let s) {
        Object value = (s.initializer != null) ? evaluate(s.initializer) : null;
        currentEnvironment.define(s.variableName.lexeme, value);
    }

    private void executeBlock(Stmt.Block s) {
        Environment previousEnvironment = currentEnvironment;
        currentEnvironment = new Environment(currentEnvironment);
        try {
            for (Stmt subStmt : s.subStatements) execute(subStmt);
        } finally {
            currentEnvironment = previousEnvironment;
        }
    }

    private void executeIf(Stmt.If s) {
        if (isTrue(evaluate(s.condition))) {
            execute(s.thenStatement);
        } else if (s.elseStatement != null) {
            execute(s.elseStatement);
        }
    }

    private void executeWhile(Stmt.While s) {
        while (isTrue(evaluate(s.condition))) {
            try {
                execute(s.subStatement);
            } catch (Break b) {
                break;
            }
        }
    }

    private void executeDef(Stmt.Def s) {
        Function function = new Function(s, currentEnvironment, false);
        currentEnvironment.define(s.functionName.lexeme, function);
    }

    private void executeReturn(Stmt.Return s) {
        Object value = (s.expression != null) ? evaluate(s.expression) : null;
        throw new Return(s.keyword, value);
    }

    private void executeClass(Stmt.Class s) {
        Object superclass = null;
        if (s.superclassExpression != null) {
            superclass = evaluate(s.superclassExpression);
            if (!(superclass instanceof Class)) {
                throw new RuntimeError("Superclass must be a class.", s.superclassExpression.identifier.line);
            }
        }
        currentEnvironment.define(s.className.lexeme, null);
        if (superclass != null) {
            currentEnvironment = new Environment(currentEnvironment);
            currentEnvironment.define("super", superclass);
        }
        Map<String, Function> methods = new HashMap<>();
        for (Stmt.Def method : s.methods) {
            boolean isConstructor = method.functionName.lexeme.equals("constructor");
            methods.put(method.functionName.lexeme, new Function(method, currentEnvironment, isConstructor));
        }
        var myClass = new Class(s.className.lexeme, methods, (Class) superclass);
        if (superclass != null) currentEnvironment = currentEnvironment.enclosingEnvironment;
        if (currentEnvironment != null) currentEnvironment.updateValue(s.className, myClass);
    }

    private Object evaluateUnary(Expr.Unary e) {
        Object value = evaluate(e.expression);
        return switch (e.operator.type) {
            case TokenType.MINUS -> -asDouble(value, e.operator);
            case TokenType.NOT -> !isTrue(value);
            default -> null; // Unreachable
        };
    }

    private Object evaluateBinary(Expr.Binary e) {
        Object leftValue = evaluate(e.leftExpression);
        return switch (e.operator.type) {
            case TokenType.STAR -> {
                Object rightValue = evaluate(e.rightExpression);
                yield asDouble(leftValue, e.operator) * asDouble(rightValue, e.operator);
            }
            case TokenType.SLASH -> {
                double rightValue = asDouble(evaluate(e.rightExpression), e.operator);
                if (rightValue == 0) throw new RuntimeError("Can not divide a number by zero.", e.operator.line);
                yield asDouble(leftValue, e.operator) / rightValue;
            }
            case TokenType.PLUS -> {
                Object rightValue = evaluate(e.rightExpression);
                if (leftValue instanceof Double l && rightValue instanceof Double r) yield l + r;
                if (leftValue instanceof String || rightValue instanceof String) yield stringify(leftValue) + stringify(rightValue);
                throw new RuntimeError("Operands must be numbers or strings.", e.operator.line);
            }
            case TokenType.MINUS -> {
                Object rightValue = evaluate(e.rightExpression);
                yield asDouble(leftValue, e.operator) - asDouble(rightValue, e.operator);
            }
            case TokenType.LESS -> {
                Object rightValue = evaluate(e.rightExpression);
                yield asDouble(leftValue, e.operator) < asDouble(rightValue, e.operator);
            }
            case TokenType.LESS_EQUAL -> {
                Object rightValue = evaluate(e.rightExpression);
                yield asDouble(leftValue, e.operator) <= asDouble(rightValue, e.operator);
            }
            case TokenType.GREATER -> {
                Object rightValue = evaluate(e.rightExpression);
                yield asDouble(leftValue, e.operator) > asDouble(rightValue, e.operator);
            }
            case TokenType.GREATER_EQUAL -> {
                Object rightValue = evaluate(e.rightExpression);
                yield asDouble(leftValue, e.operator) >= asDouble(rightValue, e.operator);
            }
            case TokenType.EQUAL_EQUAL -> areEqual(leftValue, evaluate(e.rightExpression));
            case TokenType.NOT_EQUAL -> !areEqual(leftValue, evaluate(e.rightExpression));
            case TokenType.AND -> !isTrue(leftValue) ? leftValue : evaluate(e.rightExpression);
            case TokenType.OR -> isTrue(leftValue) ? leftValue : evaluate(e.rightExpression);
            default -> null; // Unreachable
        };
    }

    private Object evaluateCall(Expr.Call e) {
        Object callee = evaluate(e.calleeExpression);
        var arguments = new ArrayList<>();
        for (Expr arg : e.arguments) arguments.add(evaluate(arg));
        if (!(callee instanceof Callable callable)) {
            throw new RuntimeError("Only functions or classes can be called.", e.leftParenthesis.line);
        }
        if (callable.arity() != arguments.size()) {
            throw new RuntimeError("Expected " + callable.arity() + " arguments but got " + arguments.size() + ".", e.leftParenthesis.line);
        }
        return callable.call(arguments, this);
    }

    private Object evaluateGet(Expr.Get e) {
        Object callee = evaluate(e.calleeExpression);
        if (callee instanceof Instance instance) return instance.get(e.property);
        throw new RuntimeError("Only instances have properties.", e.property.line);
    }

    private Object evaluateSet(Expr.Set e) {
        Object callee = evaluate(e.calleeExpression);
        if (callee instanceof Instance instance) {
            Object newValue = evaluate(e.expression);
            instance.set(e.property, newValue);
            return newValue;
        }
        throw new RuntimeError("Only instances have properties.", e.property.line);
    }

    private Object evaluateSuper(Expr.Super e) {
        var superclass = (Class) currentEnvironment.getAt("super", e.distance);
        var instance = (Instance) currentEnvironment.getAt("this", e.distance - 1);
        var method = superclass.getMethod(e.method.lexeme);
        if (method == null) throw new RuntimeError("Undefined method '" + e.method.lexeme + "'.", e.method.line);
        return method.bindInstance(instance);
    }

    private Object evaluateArrayDefinition(Expr.ArrayDefinition e) {
        List<Object> elements = new ArrayList<>();
        for (Expr expr : e.elements) {
            elements.add(evaluate(expr));
        }
        return elements;
    }

    private Object evaluateSubscriptGet(Expr.SubscriptGet e) {
        Object array = evaluate(e.array);
        Object index = evaluate(e.index);
        if (array instanceof List<?> list && index instanceof Double d) {
            return list.get(d.intValue());
        }
        throw new RuntimeError("Only arrays can be sub scripted.", e.leftBracket.line);
    }

    private Object evaluateSubscriptSet(Expr.SubscriptSet e) {
        Object array = evaluate(e.array);
        Object index = evaluate(e.index);
        Object value = evaluate(e.value);
        if (array instanceof List list && index instanceof Double d) {
            list.set(d.intValue(), value);
            return value;
        }
        throw new RuntimeError("Only arrays can be sub scripted.", e.leftBracket.line);
    }

    // Helper methods
    private double asDouble(Object operand, Token operator) {
        if (operand instanceof Double d) return d;
        throw new RuntimeError("Operand must be a number.", operator.line);
    }

    private String stringify(Object value) {
        if (value == null) return "null";
        if (value instanceof Double) {
            String number = value.toString();
            if (number.endsWith(".0")) {
                number = number.substring(0, number.length() - 2);
            }
            value = number;
        }
        return value.toString();
    }

    private boolean isTrue(Object value) {
        return switch (value) {
            case null -> false;
            case Boolean b -> b;
            case Integer i -> i != 0;
            case String s -> !s.isEmpty();
            default -> true;
        };
    }

    private boolean areEqual(Object leftValue, Object rightValue) {
        if (leftValue == null && rightValue == null) return true;
        if (leftValue == null) return false;
        return leftValue.equals(rightValue);
    }
}
