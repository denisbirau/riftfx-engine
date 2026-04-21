package ast;

import scanner.Token;

import java.util.List;

public sealed interface Stmt permits
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

    record Expression(Expr expression) implements Stmt { }

    record Let(Token variableName, Expr initializer) implements Stmt { }

    record Print(Expr expression) implements Stmt { }

    record Block(List<Stmt> subStatements) implements Stmt { }

    record If(Expr condition, Stmt thenStatement, Stmt elseStatement) implements Stmt { }

    record While(Expr condition, Stmt subStatement) implements Stmt { }

    record Break(Token keyword) implements Stmt { }

    record Def(Token name, List<Token> parameters, List<Stmt> body) implements Stmt { }

    record Return(Token keyword, Expr expression) implements Stmt { }

    record Class(Token className, List<Def> methods, Expr.Lookup superclassLookupExpression) implements Stmt { }
}
