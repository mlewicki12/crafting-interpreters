
package com.mlewicki12.lox;

public class Interpreter implements Expr.Visitor<Object> {
    private final OperatorMap operatorMap = new OperatorMap();

    void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch(RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return operatorMap.getOperator(getType(right), expr.operator.type, right);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return operatorMap.getOperator(getType(left), getType(right), expr.operator.type, left, right);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object cond = evaluate(expr.condition);
        if(isTruthy(cond)) {                    // use lox truthy check to ensure consistency
            return evaluate(expr.left);         // only evaluate the branch that are applicable
        } else return evaluate(expr.right);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private String getType(Object value) {
        if(value == null) return "nil";

        if(value instanceof Double) return "Double";
        if(value instanceof Boolean) return "Boolean";

        return "String";
    }

    private String stringify(Object value) {
        if(value == null) return "nil";

        if(value instanceof Double) {
            String text = value.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return value.toString();
    }

    private boolean isTruthy(Object value) {
        if(value == null) return false;
        if(value instanceof Boolean) return (boolean)value;
        return true;
    }
}
