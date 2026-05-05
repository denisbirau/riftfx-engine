package com.riftfx.ast;

import com.riftfx.resolution.Resolution;
import com.riftfx.scanner.Token;

import java.util.List;

public sealed interface Expr permits Expr.ArrayDefinition, Expr.Assignment, Expr.Binary, Expr.Call, Expr.GetMember,
        Expr.Group, Expr.Lambda, Expr.Literal, Expr.Lookup, Expr.ObjectLiteral, Expr.SetMember, Expr.SubscriptGet,
        Expr.SubscriptSet, Expr.Super, Expr.Ternary, Expr.This, Expr.Unary {
    record Literal(Object value) implements Expr {
    }

    record Unary(Token operator, Expr subExpression) implements Expr {
    }

    record Binary(Expr leftExpression, Token operator, Expr rightExpression) implements Expr {
    }

    record Ternary(Expr condition, Expr thenExpression, Expr elseExpression) implements Expr {
    }

    record Group(Expr innerExpression) implements Expr {
    }

    record Lookup(Token identifierToken, Resolution resolution) implements Expr {
        public Lookup(Token identifierToken) {
            this(identifierToken, new Resolution());
        }
    }

    record Assignment(Token assigneeIdentifierToken, Expr expressionToAssign, Resolution resolution) implements Expr {
        public Assignment(Token identifierToken, Expr expressionToAssign) {
            this(identifierToken, expressionToAssign, new Resolution());
        }
    }

    record Argument(Token nameToken, Expr value) {
    }

    record Call(Expr calleeExpression, Token leftParenthesis, List<Argument> arguments) implements Expr {
    }

    record GetMember(Expr objectExpression, Token memberIdentifier) implements Expr {
    }

    record SetMember(Expr objectExpression, Token memberIdentifier, Expr expressionToAssign) implements Expr {
    }

    record This(Token keyword, Resolution resolution) implements Expr {
        public This(Token keyword) {
            this(keyword, new Resolution());
        }
    }

    record Super(Token keyword, Token memberIdentifier, Resolution resolution) implements Expr {
        public Super(Token keyword, Token memberIdentifier) {
            this(keyword, memberIdentifier, new Resolution());
        }
    }

    record ArrayDefinition(List<Expr> elements) implements Expr {
    }

    record SubscriptGet(Expr sequenceExpression, Token leftBracket, Expr indexExpression) implements Expr {
    }

    record SubscriptSet(Expr sequenceExpression, Token leftBracket, Expr indexExpression, Expr expressionToAssign)
            implements Expr {
    }

    record Lambda(List<Token> parameters, List<Stmt> lambdaBody) implements Expr {
    }

    record Property(Token name, Expr value) {
    }

    record ObjectLiteral(List<Property> properties) implements Expr {
    }
}
