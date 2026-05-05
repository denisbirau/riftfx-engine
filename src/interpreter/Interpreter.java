package interpreter;

import ast.Expr;
import ast.Stmt;
import error.ErrorReporter;
import scanner.Token;
import scanner.TokenType;
import error.RuntimeError;
import stdlib.types.NativeArray;
import stdlib.core.NativeObject;
import stdlib.core.StandardLibrary;

import java.util.*;

public class Interpreter {
    private final Environment globalEnvironment = new Environment();
    Environment currentEnvironment = globalEnvironment;

    private final List<Stmt> statements;
    public final ErrorReporter errorReporter;
    public final UIRenderer renderer;

    public Interpreter(List<Stmt> statements, ErrorReporter errorReporter, UIRenderer renderer) {
        this.statements = statements;
        this.errorReporter = errorReporter;
        this.renderer = renderer;
        StandardLibrary.GLOBALS.forEach(globalEnvironment::define);
    }

    public void interpret() {
        for (var statement : statements) {
            try {
                execute(statement);
            } catch (RuntimeError error) {
                errorReporter.report(error.getMessage(), error.getToken());
                return;
            }
        }
    }

    void execute(Stmt stmt) {
        switch (stmt) {
            case Stmt.Expression s -> executeExpressionStatement(s);
            case Stmt.Let s        -> executeLetStatement(s);
            case Stmt.Print s      -> executePrintStatement(s);
            case Stmt.Block s      -> executeBlockStatement(s);
            case Stmt.If s         -> executeIfStatement(s);
            case Stmt.While s      -> executeWhileStatement(s);
            case Stmt.Break s      -> executeBreakStatement(s);
            case Stmt.Def s        -> executeDefStatement(s);
            case Stmt.Return s     -> executeReturnStatement(s);
            case Stmt.Class s      -> executeClassStatement(s);
        }
    }

    private Object evaluate(Expr expr) {
        return switch (expr) {
            case Expr.Literal e         -> evaluateLiteralExpression(e);
            case Expr.Unary e           -> evaluateUnaryExpression(e);
            case Expr.Binary e          -> evaluateBinaryExpression(e);
            case Expr.Ternary e         -> evaluateTernaryExpression(e);
            case Expr.Group e           -> evaluateGroupExpression(e);
            case Expr.Lookup e          -> evaluateLookupExpression(e);
            case Expr.Assignment e      -> evaluateAssignmentExpression(e);
            case Expr.Call e            -> evaluateCallExpression(e);
            case Expr.GetMember e       -> evaluateGetExpression(e);
            case Expr.SetMember e       -> evaluateSetExpression(e);
            case Expr.This e            -> evaluateThisExpression(e);
            case Expr.Super e           -> evaluateSuperExpression(e);
            case Expr.ArrayDefinition e -> evaluateArrayDefinitionExpression(e);
            case Expr.SubscriptGet e    -> evaluateSubscriptGetExpression(e);
            case Expr.SubscriptSet e    -> evaluateSubscriptSetExpression(e);
            case Expr.Lambda e          -> evaluateLambdaExpression(e);
        };
    }

    // Statement Handlers
    private void executeExpressionStatement(Stmt.Expression stmt) {
        evaluate(stmt.expression());
    }

    private void executeLetStatement(Stmt.Let stmt) {
        var value = (stmt.initializer() != null) ? evaluate(stmt.initializer()) : null;
        currentEnvironment.define(stmt.variableName().lexeme(), value);
    }

    private void executePrintStatement(Stmt.Print stmt) {
        System.out.println(stringify(evaluate(stmt.expression())));
    }

    private void executeBlockStatement(Stmt.Block stmt) {
        var previousEnvironment = currentEnvironment;
        currentEnvironment = new Environment(currentEnvironment);
        try {
            for (var subStmt : stmt.subStatements()) {
                execute(subStmt);
            }
        } finally {
            currentEnvironment = previousEnvironment;
        }
    }

    private void executeIfStatement(Stmt.If stmt) {
        if (isTrue(evaluate(stmt.condition()))) {
            execute(stmt.thenStatement());
        } else if (stmt.elseStatement() != null) {
            execute(stmt.elseStatement());
        }
    }

    private void executeWhileStatement(Stmt.While stmt) {
        while (isTrue(evaluate(stmt.condition()))) {
            try {
                execute(stmt.subStatement());
            } catch (Break b) {
                break;
            }
        }
    }

    private void executeBreakStatement(Stmt.Break stmt) {
        throw new Break(stmt.keyword().line());
    }

    private void executeDefStatement(Stmt.Def stmt) {
        var function = new Function(
                stmt.name().lexeme(),
                stmt.parameters(),
                stmt.body(),
                currentEnvironment,
                false
        );
        currentEnvironment.define(stmt.name().lexeme(), function);
    }

    private void executeReturnStatement(Stmt.Return stmt) {
        var value = (stmt.expression() != null) ? evaluate(stmt.expression()) : null;
        throw new Return(stmt.keyword(), value);
    }

    private void executeClassStatement(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclassLookupExpression() != null) {
            superclass = evaluate(stmt.superclassLookupExpression());
            if (!(superclass instanceof Class)) {
                throw new RuntimeError("Superclass must be a class.", stmt.superclassLookupExpression().identifierToken());
            }
        }
        currentEnvironment.define(stmt.className().lexeme(), null);
        if (superclass != null) {
            currentEnvironment = new Environment(currentEnvironment);
            currentEnvironment.define("super", superclass);
        }
        try {
            var methods = new HashMap<String, Function>();
            for (var method : stmt.methods()) {
                boolean isConstructor = method.name().lexeme().equals("constructor");
                methods.put(method.name().lexeme(), new Function(
                        method.name().lexeme(),
                        method.parameters(),
                        method.body(),
                        currentEnvironment,
                        isConstructor
                ));
            }
            var myClass = new Class(stmt.className().lexeme(), methods, (Class) superclass);
            (superclass != null ? currentEnvironment.enclosingEnvironment : currentEnvironment)
                    .updateGlobal(stmt.className(), myClass);
        } finally {
            if (superclass != null) {
                currentEnvironment = currentEnvironment.enclosingEnvironment;
            }
        }
    }

    // Expression Handlers
    private Object evaluateLiteralExpression(Expr.Literal expr) {
        return expr.value();
    }

    private Object evaluateUnaryExpression(Expr.Unary expr) {
        var value = evaluate(expr.subExpression());
        return switch (expr.operator().type()) {
            case TokenType.MINUS -> -asDouble(value, expr.operator());
            case TokenType.NOT -> !isTrue(value);
            default -> null; // Unreachable
        };
    }

    private Object evaluateBinaryExpression(Expr.Binary expr) {
        var leftValue = evaluate(expr.leftExpression());
        return switch (expr.operator().type()) {
            case TokenType.STAR -> {
                var rightValue = evaluate(expr.rightExpression());
                yield asDouble(leftValue, expr.operator()) * asDouble(rightValue, expr.operator());
            }
            case TokenType.SLASH -> {
                var rightValue = asDouble(evaluate(expr.rightExpression()), expr.operator());
                if (rightValue == 0) {
                    throw new RuntimeError("Can not divide a number by zero.", expr.operator());
                }
                yield asDouble(leftValue, expr.operator()) / rightValue;
            }
            case TokenType.MODULO -> {
                var rightValue = asDouble(evaluate(expr.rightExpression()), expr.operator());
                if (rightValue == 0) {
                    throw new RuntimeError("Can not modulo a number by zero.", expr.operator());
                }
                yield asDouble(leftValue, expr.operator()) % rightValue;
            }
            case TokenType.PLUS -> {
                var rightValue = evaluate(expr.rightExpression());
                yield switch (leftValue) {
                    case Double l when rightValue instanceof Double r -> l + r;
                    case String s -> s + stringify(rightValue);
                    case Object l when rightValue instanceof String r -> stringify(l) + r;
                    default -> throw new RuntimeError("Operands must be numbers or strings.", expr.operator());
                };
            }
            case TokenType.MINUS -> {
                var rightValue = evaluate(expr.rightExpression());
                yield asDouble(leftValue, expr.operator()) - asDouble(rightValue, expr.operator());
            }
            case TokenType.LESS -> {
                var rightValue = evaluate(expr.rightExpression());
                yield asDouble(leftValue, expr.operator()) < asDouble(rightValue, expr.operator());
            }
            case TokenType.LESS_EQUAL -> {
                var rightValue = evaluate(expr.rightExpression());
                yield asDouble(leftValue, expr.operator()) <= asDouble(rightValue, expr.operator());
            }
            case TokenType.GREATER -> {
                var rightValue = evaluate(expr.rightExpression());
                yield asDouble(leftValue, expr.operator()) > asDouble(rightValue, expr.operator());
            }
            case TokenType.GREATER_EQUAL -> {
                var rightValue = evaluate(expr.rightExpression());
                yield asDouble(leftValue, expr.operator()) >= asDouble(rightValue, expr.operator());
            }
            case TokenType.EQUAL_EQUAL -> Objects.equals(leftValue, evaluate(expr.rightExpression()));
            case TokenType.NOT_EQUAL -> !Objects.equals(leftValue, evaluate(expr.rightExpression()));
            case TokenType.AND -> !isTrue(leftValue) ? leftValue : evaluate(expr.rightExpression());
            case TokenType.OR -> isTrue(leftValue) ? leftValue : evaluate(expr.rightExpression());
            default -> throw new IllegalStateException("Unexpected binary operator: " + expr.operator().type());
        };
    }

    private Object evaluateTernaryExpression(Expr.Ternary expr) {
        return isTrue(evaluate(expr.condition())) ? evaluate(expr.thenExpression()) : evaluate(expr.elseExpression());
    }

    private Object evaluateGroupExpression(Expr.Group expr) {
        return evaluate(expr.innerExpression());
    }

    private Object evaluateLookupExpression(Expr.Lookup expr) {
        if (expr.resolution().distance != null) {
            return currentEnvironment.getAt(expr.identifierToken().lexeme(), expr.resolution().distance);
        } else {
            return globalEnvironment.getGlobal(expr.identifierToken());
        }
    }

    private Object evaluateAssignmentExpression(Expr.Assignment expr) {
        var valueToAssign = evaluate(expr.expressionToAssign());
        if (expr.resolution().distance != null) {
            currentEnvironment.updateAt(expr.assigneeIdentifierToken().lexeme(), expr.resolution().distance, valueToAssign);
        } else {
            globalEnvironment.updateGlobal(expr.assigneeIdentifierToken(), valueToAssign);
        }
        return valueToAssign;
    }

    private Object evaluateCallExpression(Expr.Call expr) {
        var callee = evaluate(expr.calleeExpression());
        if (!(callee instanceof Callable callable)) {
            throw new RuntimeError("Only functions or classes can be called.", expr.leftParenthesis());
        }
        if (!callable.acceptsArity(expr.arguments().size())) {
            throw new RuntimeError("Invalid number of arguments. Expected " + callable.arity() + " but got " + expr.arguments().size() + ".", expr.leftParenthesis());
        }

        var arguments = resolveArguments(callable, expr);

        try {
            return callable.call(arguments, this);
        } catch (RuntimeError error) {
            throw error;
        } catch (RuntimeException error) {
            throw new RuntimeError(error.getMessage(), expr.leftParenthesis());
        }
    }

    private List<Object> resolveArguments(Callable callable, Expr.Call expr) {
        var arguments = new ArrayList<>();
        var isFilled = new boolean[callable.arity()]; // Tracks filled parameter slots
        for (var i = 0; i < callable.arity(); i++) {
            arguments.add(null);
        }

        var positionalIndex = 0;
        var parameterNames = callable.parameterNames();
        var namedArgumentSeen = false;

        for (var i = 0; i < expr.arguments().size(); i++) {
            var currentArgument = expr.arguments().get(i);
            var currentValue = evaluate(currentArgument.value());

            int targetIndex;

            if (currentArgument.nameToken() != null) {
                // 1. Named Argument
                namedArgumentSeen = true;
                targetIndex = parameterNames.indexOf(currentArgument.nameToken().lexeme());
                if (targetIndex == -1) {
                    throw new RuntimeError("No parameter named '" + currentArgument.nameToken().lexeme() + "' found.", currentArgument.nameToken());
                }
            } else {
                // 2. Positional Argument / Trailing Lambda
                var isTrailingLambda = (i == expr.arguments().size() - 1) && (currentArgument.value() instanceof Expr.Lambda);

                if (isTrailingLambda) {
                    // Trailing lambdas always bind to the LAST parameter
                    targetIndex = callable.arity() - 1;
                } else {
                    if (namedArgumentSeen) {
                        throw new RuntimeError("Positional arguments can not appear after named arguments.", expr.leftParenthesis());
                    }
                    if (positionalIndex >= callable.arity()) {
                        throw new RuntimeError("Too many positional arguments provided.", expr.leftParenthesis());
                    }
                    targetIndex = positionalIndex;
                    positionalIndex++;
                }
            }

            // Detect collisions
            if (isFilled[targetIndex]) {
                var errorToken = currentArgument.nameToken() != null ? currentArgument.nameToken() : expr.leftParenthesis();
                var collisionName = parameterNames.size() > targetIndex ? parameterNames.get(targetIndex) : "index " + targetIndex;
                throw new RuntimeError("Multiple values passed for parameter '" + collisionName + "'.", errorToken);
            }

            arguments.set(targetIndex, currentValue);
            isFilled[targetIndex] = true;
        }
        return arguments;
    }

    private Object evaluateGetExpression(Expr.GetMember expr) {
        var callee = evaluate(expr.objectExpression());
        if (callee instanceof Instance instance) {
            return instance.get(expr.memberIdentifier());
        }
        if (callee instanceof NativeObject nativeObj) {
            try {
                return nativeObj.getMember(expr.memberIdentifier());
            } catch (RuntimeException e) {
                throw new RuntimeError(e.getMessage(), expr.memberIdentifier());
            }
        }
        if (callee instanceof String str) {
            switch (expr.memberIdentifier().lexeme()) {
                case "len" -> {
                    return (double) str.length();
                }
                case "toUpperCase" -> {
                    return new Callable() {
                        @Override
                        public int arity() {
                            return 0;
                        }

                        @Override
                        public Object call(List<Object> arguments, Interpreter interpreter) {
                            return str.toUpperCase();
                        }
                    };
                }
                case "toLowerCase" -> {
                    return new Callable() {
                        @Override
                        public int arity() {
                            return 0;
                        }

                        @Override
                        public Object call(List<Object> arguments, Interpreter interpreter) {
                            return str.toLowerCase();
                        }
                    };
                }
            }
        }
        throw new RuntimeError("Only instances have properties.", expr.memberIdentifier());
    }

    private Object evaluateSetExpression(Expr.SetMember expr) {
        var callee = evaluate(expr.objectExpression());
        var newValue = evaluate(expr.expressionToAssign());
        if (callee instanceof Instance instance) {
            instance.set(expr.memberIdentifier(), newValue);
            return newValue;
        }
        if (callee instanceof NativeObject nativeObject) {
            try {
                nativeObject.setMember(expr.memberIdentifier(), newValue);
                return newValue;
            } catch (RuntimeException e) {
                throw new RuntimeError(e.getMessage(), expr.memberIdentifier());
            }
        }
        throw new RuntimeError("Only instances have properties.", expr.memberIdentifier());
    }

    private Object evaluateThisExpression(Expr.This expr) {
        return currentEnvironment.getAt("this", expr.resolution().distance);
    }

    private Object evaluateSuperExpression(Expr.Super expr) {
        var superclass = (Class) currentEnvironment.getAt("super", expr.resolution().distance);
        var instance = (Instance) currentEnvironment.getAt("this", expr.resolution().distance - 1);
        var method = superclass.getMethod(expr.memberIdentifier().lexeme());
        if (method == null) {
            throw new RuntimeError("Undefined method '" + expr.memberIdentifier().lexeme() + "'.", expr.memberIdentifier());
        }
        return method.bindInstance(instance);
    }

    private Object evaluateArrayDefinitionExpression(Expr.ArrayDefinition expr) {
        var elements = new ArrayList<>();
        for (var element : expr.elements()) {
            elements.add(evaluate(element));
        }
        return new NativeArray(elements);
    }

    private Object evaluateSubscriptGetExpression(Expr.SubscriptGet expr) {
        var array = evaluate(expr.sequenceExpression());
        var index = evaluate(expr.indexExpression());
        if (array instanceof NativeArray(List<Object> elements) && index instanceof Double d) {
            var i = d.intValue();
            if (i < 0 || i >= elements.size()) {
                throw new RuntimeError("Array index out of bounds.", expr.leftBracket());
            }
            return elements.get(i);
        }
        if (array instanceof String str && index instanceof Double d) {
            var i = d.intValue();
            if (i < 0 || i >= str.length()) {
                throw new RuntimeError("String index out of bounds.", expr.leftBracket());
            }
            return String.valueOf(str.charAt(i)); // Returns the character as a new String
        }
        throw new RuntimeError("Only arrays can be sub scripted.", expr.leftBracket());
    }

    private Object evaluateSubscriptSetExpression(Expr.SubscriptSet expr) {
        var array = evaluate(expr.sequenceExpression());
        var index = evaluate(expr.indexExpression());
        var value = evaluate(expr.expressionToAssign());
        if (array instanceof NativeArray(List<Object> elements) && index instanceof Double d) {
            var i = d.intValue();
            if (i < 0 || i >= elements.size()) {
                throw new RuntimeError("Array index out of bounds.", expr.leftBracket());
            }
            elements.set(i, value);
            return value;
        }
        throw new RuntimeError("Only arrays can be sub scripted.", expr.leftBracket());
    }

    private Object evaluateLambdaExpression(Expr.Lambda expr) {
        return new Function(null, expr.parameters(), expr.lambdaBody(), currentEnvironment, false);
    }

    /**
     * @param operand Value to convert to Double
     * @param operator Error reporting
     * @return Operant converted to Double
     * @throws RuntimeError If operand is not a number
     */
    // Helper methods
    private double asDouble(Object operand, Token operator) {
        if (operand instanceof Double d) {
            return d;
        }
        throw new RuntimeError("Operand must be a number.", operator);
    }

    private String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Double) {
            var number = value.toString();
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
}
