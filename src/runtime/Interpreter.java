package runtime;

import ast.Expr;
import ast.Stmt;
import error.IErrorReporter;
import parsing.Token;
import parsing.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter {
    Environment currentEnvironment;
    private final Map<Expr, Integer> identifiersDistances = new HashMap<>();
    private final List<Stmt> statements;
    private final IErrorReporter errorReporter;

    public Interpreter(List<Stmt> statements, IErrorReporter errorReporter) {
        this.statements = statements;
        this.errorReporter = errorReporter;
        currentEnvironment = new Environment();

        // Native functions
        this.currentEnvironment.define("isKeyDown", new NativeFunctionIsKeyDown());
        this.currentEnvironment.define("drawRect", new NativeFunctionDrawRect());
        this.currentEnvironment.define("drawText", new NativeFunctionDrawText());
    }

    public void callScriptFunction(String identifier) {
        Object function = currentEnvironment.getValue(identifier);
        if (function instanceof Callable callable) {
            callable.call(List.of(), this);
        }
    }

    public Object getGlobalVariable(String identifier) {
        return currentEnvironment.getValue(identifier);
    }

    public void addIdentifierDistance(Expr expr, int depth) {
        identifiersDistances.put(expr, depth);
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
            case Expr.Literal e    -> e.value;
            case Expr.Unary e      -> evaluateUnary(e);
            case Expr.Binary e     -> evaluateBinary(e);
            case Expr.Ternary e    -> isTrue(evaluate(e.condition)) ? evaluate(e.thenExpression) : evaluate(e.elseExpression);
            case Expr.Group e      -> evaluate(e.expression);
            case Expr.Lookup e     -> currentEnvironment.getAt(e.identifier.lexeme, identifiersDistances.get(e));
            case Expr.Assignment e -> currentEnvironment.updateAt(e.identifier, evaluate(e.expression), identifiersDistances.get(e));
            case Expr.Call e       -> evaluateCall(e);
            case Expr.Get e        -> evaluateGet(e);
            case Expr.Set e        -> evaluateSet(e);
            case Expr.This e       -> currentEnvironment.getAt("this", identifiersDistances.get(e));
            case Expr.Super e      -> evaluateSuper(e);
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
        int distance = identifiersDistances.get(e);
        var superclass = (Class) currentEnvironment.getAt("super", distance);
        var instance = (Instance) currentEnvironment.getAt("this", distance - 1);
        var method = superclass.getMethod(e.method.lexeme);
        if (method == null) throw new RuntimeError("Undefined method '" + e.method.lexeme + "'.", e.method.line);
        return method.bindInstance(instance);
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
