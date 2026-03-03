package runtime;

import ast.Stmt;

import java.util.List;

class Function implements Callable {
    private final Stmt.Def functionStatement;
    private final Environment environment;
    private final boolean isConstructor;

    Function(Stmt.Def functionStatement, Environment environment, boolean isConstructor) {
        this.functionStatement = functionStatement;
        this.environment = environment;
        this.isConstructor = isConstructor;
    }

    Function bindInstance(Instance instance) {
        Environment environment = new Environment(this.environment);
        environment.define("this", instance);
        return new Function(functionStatement, environment, isConstructor);
    }

    @Override
    public String toString() {
        return functionStatement.functionName.lexeme;
    }

    @Override
    public int arity() {
        return functionStatement.parameters.size();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Environment newEnvironment = new Environment(this.environment);
        for (int i = 0; i < arguments.size(); i++) {
            newEnvironment.define(functionStatement.parameters.get(i).lexeme, arguments.get(i));
        }

        Environment previous = interpreter.currentEnvironment;
        interpreter.currentEnvironment = newEnvironment;
        for (Stmt statement : functionStatement.functionBody) {
            try {
                interpreter.execute(statement);
            } catch (Return returnStmt) {
                interpreter.currentEnvironment = previous;
                return returnStmt.value;
            }
        }
        interpreter.currentEnvironment = previous;
        if (isConstructor) return this.environment.getValue("this");
        return null;
    }
}
