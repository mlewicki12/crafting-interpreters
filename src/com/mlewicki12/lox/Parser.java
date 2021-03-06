
package com.mlewicki12.lox;

import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {                           // make sure any errors don't escape the parser
            return expression();        // enter recursive descent
        } catch(ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return errorprod();              // run through an error production to make sure there isn't a lonely :,( operator
    }

    private Expr errorprod() {
        if(match(TokenType.COMMA, TokenType.QUESTION_MARK, TokenType.COLON, TokenType.BANG_EQUAL,
                 TokenType.EQUAL_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL,
                 TokenType.PLUS, TokenType.SLASH, TokenType.STAR)) {
            throw error(previous(), "expected expression");         // this doesn't parse the next expression, I think I would have to do something
                                                                            // in the try catch in parse for that
        } else {
            return comma();
        }
    }

    private Expr comma() {
        Expr expr = conditional();

        while(match(TokenType.COMMA)) {
            Token operator = previous();    // technically shouldn't be needed, but the token will have more relevant info
            Expr right = conditional();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr conditional() {
        Expr expr = equality();

        while(match(TokenType.QUESTION_MARK)) {                             // structure right now should be equality ( ? conditional : conditional )*
            Expr left = conditional();
            consume(TokenType.COLON, "expected ':' after '?'");
            Expr right = conditional();
            expr = new Expr.Ternary(expr, left, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();                                   // parse first half of expression as a comparison as it's of higher precendence

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) { // match equalities until we can't match anymore
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);          // recursively build it into the tree
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if(match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.NIL)) return new Expr.Literal(null);
        if(match(TokenType.EXIT)) return new Expr.Exit(previous());

        if(match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "expected ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "expected expression");
    }

    private boolean match(TokenType... types) {
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private void synchronize() {
        advance();

        while(!isAtEnd()) {
            if(previous().type == TokenType.SEMICOLON) return;

            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Token consume(TokenType expected, String message) {
        if(check(expected)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
