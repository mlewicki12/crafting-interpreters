
package com.mlewicki12.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if(args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);  // invalid argument exit
                                    // https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
        } else if(args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path)); // scan in the provided file
        run(new String(bytes, Charset.defaultCharset()));   // throw it in our interpreter

        if(hadError) System.exit(65);                 // exit DATAERR if the input was wrong
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);          // create an input reader

        for(;;) {
            System.out.print("> ");                                 // format repl line
            String line = reader.readLine();                        // read the input line
            if(line == null) break;                                 // ctrl+d sends null to readline, so break on that
            run(line);                                              // throw it in our interpreter
            hadError = false;                                       // reset the error loop bc we're not running a whole file
        }
    }

    private static void run(String source) throws IOException {
        Scanner scanner = new Scanner(source);      // create a scanner object
        List<Token> tokens = scanner.scanTokens();  // scan the source into tokens

        Parser parser = new Parser(tokens);         // create a new parser and parse the input
        Expr expression = parser.parse();

        if(hadError) return;                        // make sure we don't print if there was an error

        System.out.println(new AstPrinter().print(expression, false));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if(token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at " + token.lexeme, message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println(String.format("[%d] jlox error %s: %s", line, where, message));
        hadError = true;                                                                    // ensure that, unlike myself, jlox doesn't run past errors
    }
}
