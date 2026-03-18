package ast;

import compiler.Token;

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
    }

    public static final class Let extends Stmt {
        public final Token variableName;
        public final Expr initializer;

        public Let(Token variableName, Expr initializer) {
            this.variableName = variableName;
            this.initializer = initializer;
        }
    }

    public static final class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }
    }

    public static final class Block extends Stmt {
        public final List<Stmt> subStatements;

        public Block(List<Stmt> subStatements) {
            this.subStatements = subStatements;
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
    }

    public static final class While extends Stmt {
        public final Expr condition;
        public final Stmt subStatement;

        public While(Expr condition, Stmt subStatement) {
            this.condition = condition;
            this.subStatement = subStatement;
        }
    }

    public static final class Break extends Stmt {
        public final Token keyword;

        public Break(Token keyword) {
            this.keyword = keyword;
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
    }

    public static final class Return extends Stmt {
        public final Token keyword;
        public final Expr expression;

        public Return(Token keyword, Expr expression) {
            this.keyword = keyword;
            this.expression = expression;
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
    }
}
