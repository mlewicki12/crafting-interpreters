
package com.mlewicki12.lox;

public class AstPrinter implements Expr.Visitor<String> {
    private final boolean rpn = true;

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(123)
            ),

            new Token(TokenType.STAR, "*", null, 1),

            new Expr.Grouping(
                new Expr.Literal(45.67)
            )
        );

        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, rpn, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", rpn, expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, rpn, expr.right);
    }

    private String parenthesize(String name, boolean rpn, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        if(rpn) {
            for(Expr expr : exprs) {
                builder.append(expr.accept(this));
                builder.append(" ");
            }

            builder.append(name);
            return builder.toString();
        }

        builder.append("(").append(name);
        for(Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }
}
