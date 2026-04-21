package interpreter;

import ast.Stmt;
import scanner.Token;

import java.util.List;

class Function implements Callable {
    private final String name;
    private final List<Token> parameters;
    private final List<Stmt> body;
    private final Environment environment;
    private final boolean isConstructor;

    Function(String name, List<Token> parameters, List<Stmt> body, Environment environment, boolean isConstructor) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.environment = environment;
        this.isConstructor = isConstructor;
    }

    Function bindInstance(Instance instance) {
        Environment environment = new Environment(this.environment);
        environment.define("this", instance);
        return new Function(name, parameters, body, environment, isConstructor);
    }

    @Override
    public String toString() {
        return name != null ? "<fn " + name + ">" : "<fn lambda>";
    }

    @Override
    public int arity() {
        return parameters.size();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Environment newEnvironment = new Environment(this.environment);
        for (var i = 0; i < arguments.size(); i++) {
            newEnvironment.define(parameters.get(i).lexeme(), arguments.get(i));
        }

        Environment previousEnvironment = interpreter.currentEnvironment;
        interpreter.currentEnvironment = newEnvironment;

        try {
            for (var statement : body) {
                interpreter.execute(statement);
            }
        } catch (Return returnStatement) {
            if (isConstructor) {
                return this.environment.getAt("this", 0);
            }
            return returnStatement.value;
        } finally {
            interpreter.currentEnvironment = previousEnvironment;
        }

        if (isConstructor) {
            return this.environment.getAt("this", 0);
        }

        return null;
    }
}
