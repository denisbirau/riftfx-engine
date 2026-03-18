package ast;

import compiler.Token;

import java.util.List;

public sealed abstract class Expr permits
        Expr.Literal,
        Expr.Unary,
        Expr.Binary,
        Expr.Ternary,
        Expr.Group,
        Expr.Lookup,
        Expr.Assignment,
        Expr.Call,
        Expr.Get,
        Expr.Set,
        Expr.This,
        Expr.Super,
        Expr.ArrayDefinition,
        Expr.SubscriptGet,
        Expr.SubscriptSet,
        Expr.Lambda
{

    public static final class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }
    }

    public static final class Unary extends Expr {
        public final Token operator;
        public final Expr expression;

        public Unary(Token operator, Expr expression) {
            this.operator = operator;
            this.expression = expression;
        }
    }

    public static final class Binary extends Expr {
        public final Expr leftExpression;
        public final Token operator;
        public final Expr rightExpression;

        public Binary(Expr leftExpression, Token operator, Expr rightExpression) {
            this.leftExpression = leftExpression;
            this.operator = operator;
            this.rightExpression = rightExpression;
        }
    }

    public static final class Ternary extends Expr {
        public final Expr condition;
        public final Expr thenExpression;
        public final Expr elseExpression;

        public Ternary(Expr condition, Expr thenExpression, Expr elseExpression) {
            this.condition = condition;
            this.thenExpression = thenExpression;
            this.elseExpression = elseExpression;
        }
    }

    public static final class Group extends Expr {
        public final Expr expression;

        public Group(Expr expression) {
            this.expression = expression;
        }
    }

    public static final class Lookup extends Expr {
        public final Token identifier;
        public Integer distance = null;

        public Lookup(Token identifier) {
            this.identifier = identifier;
        }
    }

    public static final class Assignment extends Expr {
        public final Token identifier;
        public final Expr expression;
        public Integer distance = null;

        public Assignment(Token identifier, Expr expression) {
            this.identifier = identifier;
            this.expression = expression;
        }
    }

    public static final class Call extends Expr {
        public final Expr calleeExpression;
        public final Token leftParenthesis;
        public final List<Expr> arguments;

        public Call(Expr calleeExpression, Token leftParenthesis, List<Expr> arguments) {
            this.calleeExpression = calleeExpression;
            this.leftParenthesis = leftParenthesis;
            this.arguments = arguments;
        }
    }

    public static final class Get extends Expr {
        public final Expr calleeExpression;
        public final Token property;

        public Get(Expr calleeExpression, Token property) {
            this.calleeExpression = calleeExpression;
            this.property = property;
        }
    }

    public static final class Set extends Expr {
        public final Expr calleeExpression;
        public final Token property;
        public final Expr expression;

        public Set(Expr calleeExpression, Token property, Expr expression) {
            this.calleeExpression = calleeExpression;
            this.property = property;
            this.expression = expression;
        }
    }

    public static final class This extends Expr {
        public final Token keyword;
        public Integer distance = null;

        public This(Token keyword) {
            this.keyword = keyword;
        }
    }

    public static final class Super extends Expr {
        public final Token keyword;
        public final Token method;
        public Integer distance = null;

        public Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }
    }

    public static final class ArrayDefinition extends Expr {
        public final List<Expr> elements;

        public ArrayDefinition(List<Expr> elements) {
            this.elements = elements;
        }
    }

    public static final class SubscriptGet extends Expr {
        public final Expr array;
        public final Token leftBracket;
        public final Expr index;

        public SubscriptGet(Expr array, Token leftBracket, Expr index) {
            this.array = array;
            this.leftBracket = leftBracket;
            this.index = index;
        }
    }

    public static final class SubscriptSet extends Expr {
        public final Expr array;
        public final Token leftBracket;
        public final Expr index;
        public final Expr value;

        public SubscriptSet(Expr array, Token leftBracket, Expr index, Expr value) {
            this.array = array;
            this.leftBracket = leftBracket;
            this.index = index;
            this.value = value;
        }
    }

    public static final class Lambda extends Expr {
        public final List<Token> parameters;
        public final List<Stmt> body;

        public Lambda(List<Token> parameters, List<Stmt> body) {
            this.parameters = parameters;
            this.body = body;
        }
    }
}
