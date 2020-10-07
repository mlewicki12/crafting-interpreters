
package com.mlewicki12.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     TokenType.AND);
        keywords.put("class",   TokenType.CLASS);
        keywords.put("else",    TokenType.ELSE);
        keywords.put("false",   TokenType.FALSE);
        keywords.put("for",     TokenType.FOR);
        keywords.put("fun",     TokenType.FUN);
        keywords.put("if",      TokenType.IF);
        keywords.put("nil",     TokenType.NIL);
        keywords.put("or",      TokenType.OR);
        keywords.put("print",   TokenType.PRINT);
        keywords.put("return",  TokenType.RETURN);
        keywords.put("super",   TokenType.SUPER);
        keywords.put("this",    TokenType.THIS);
        keywords.put("true",    TokenType.TRUE);
        keywords.put("var",     TokenType.VAR);
        keywords.put("while",   TokenType.WHILE);
    }

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;                                                // update scanner position
            scanToken();                                                    // scan the next token
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));   // add eof token
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch(c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;                                                // get single character tokens
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;          // possible two character tokens
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;

            case '/':                                                                                       // special behaviour for comment
                if(match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if(match('*')) {
                    multiline();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case '"': string(); break;                                                                      // separate off scanning longer lexemes

            case ' ':                                                                                       // kill whitespace
            case '\r':
            case '\t':
                break;

            case '\n':                                                                                      // add a line when encountering a new line
                line++;
                break;

            default:
                if(isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, String.format("unexpected character %c", c));
                }

                break;
        }
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {                        // keep going until we find a closing quote
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {                                             // handle reaching EOF before finishing string
            Lox.error(line, "unterminated string");
            return;
        }

        advance();                                                  // swallow the closing "

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);                          // string off surrounding quotes
    }

    private void number() {
        while(isDigit(peek())) advance();           // eat all the numbers

        if(peek() == '.' && isDigit(peekNext())) {  // look for fractions
            advance();                              // eat that dot

            while(isDigit(peek())) advance();       // eat the rest of the numbers
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean multiline() {
        while(peek() != '*' && !isAtEnd()) {                                // same code as string, make sure we account for new lines
            if(peek() == '\n') line++;
            if(match('/')) {                                        // nested multiline comment
                if(match('*')) {
                    boolean test = multiline();
                    if (!test) {                                                 // if we failed run it up the line
                        return false;
                    }
                }
            }
            advance();
        }

        if(isAtEnd()) {
            return false;                                                   // kill the scanner if the comment doesn't close
                                                                            // doesn't particularly need to be an error, but
                                                                            // return false so we can ensure proper behaviour for multiline
        }

        advance();                                                          // swallow the closing *
        if(!match('/')) {
            return multiline();                                             // if there's no closing / we continue processing the comment
        }

        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);     // extract the lexeme from the string
        tokens.add(new Token(type, text, literal, line));   // insert into list
    }
}
