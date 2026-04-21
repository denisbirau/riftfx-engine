package error;

import scanner.Token;
import scanner.TokenType;

public class ErrorReporter {
    private static boolean hadError = false;

    public static void report(String message, int line) {
        report(line, "", message);
    }

    public static void report(String message, Token token) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    public static void report(int line, String where, String message) {
        System.err.println("Error[line " + line + "]" + where + ": " + message);
        hadError = true;
    }

    public static boolean hadError() {
        return hadError;
    }
}
