package ast;

import parsing.Token;

import java.util.List;

public sealed abstract class Stmt permits
        Stmt.Expression,
        Stmt.Let,
        Stmt.Print,
        Stmt.Block,
        Stmt.If,
        Stmt.While,
        Stmt.Break,
        Stmt.Def,
        Stmt.Return,
        Stmt.Class
{

    public static final class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "ExpressionStmt(" + expression.toString() + ")";
        }
    }

    public static final class Let extends Stmt {
        public final Token variableName;
        public final Expr initializer;

        public Let(Token variableName, Expr initializer) {
            this.variableName = variableName;
            this.initializer = initializer;
        }

        @Override
        public String toString() {
            return "VarStmt(var " + variableName + " = " + initializer + ")";
        }
    }

    public static final class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "PrintStmt(print " + expression.toString() + ")";
        }
    }

    public static final class Block extends Stmt {
        public final List<Stmt> subStatements;

        public Block(List<Stmt> subStatements) {
            this.subStatements = subStatements;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("BlockStmt(");
            for (Stmt statement : subStatements) {
                builder.append(statement.toString()).append(";");
            }
            builder.append(")");
            return builder.toString();
        }
    }

    public static final class If extends Stmt {
        public final Expr condition;
        public final Stmt thenStatement;
        public final Stmt elseStatement;

        public If(Expr condition, Stmt thenStatement, Stmt elseStatement) {
            this.condition = condition;
            this.thenStatement = thenStatement;
            this.elseStatement = elseStatement;
        }

        @Override
        public String toString() {
            return "IfStmt(if (" + condition + ") { " + thenStatement + "} else {" + elseStatement + "})";
        }
    }

    public static final class While extends Stmt {
        public final Expr condition;
        public final Stmt subStatement;

        public While(Expr condition, Stmt subStatement) {
            this.condition = condition;
            this.subStatement = subStatement;
        }

        @Override
        public String toString() {
            return "WhileStmt(while (" + condition + ") {" + subStatement + "})";
        }
    }

    public static final class Break extends Stmt {
        public final Token keyword;

        public Break(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public String toString() {
            return "BreakStmt()";
        }
    }

    public static final class Def extends Stmt {
        public final Token functionName;
        public final List<Token> parameters;
        public final List<Stmt> functionBody;

        public Def(Token functionName, List<Token> parameters, List<Stmt> functionBody) {
            this.functionName = functionName;
            this.parameters = parameters;
            this.functionBody = functionBody;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("DefStmt(");
            builder.append(functionName).append(", ");
            for (Token param : parameters) {
                builder.append(param).append(", ");
            }
            builder.append(functionBody).append(")");
            return builder.toString();
        }
    }

    public static final class Return extends Stmt {
        public final Token keyword;
        public final Expr expression;

        public Return(Token keyword, Expr expression) {
            this.keyword = keyword;
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "ReturnStmt(" + expression + ")";
        }
    }

    public static final class Class extends Stmt {
        public final Token className;
        public final List<Def> methods;
        public final Expr.Lookup superclassExpression;

        public Class(Token className, List<Def> methods, Expr.Lookup superclassExpression) {
            this.className = className;
            this.methods = methods;
            this.superclassExpression = superclassExpression;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("ClassStmt(" + className);
            for (Def method : methods) {
                builder.append(method).append(",");
            }
            builder.append(")");
            return builder.toString();
        }
    }
}
