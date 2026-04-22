package error;

import scanner.Token;
import scanner.TokenType;

public class ErrorReporter {
    private boolean hadError = false;

    public void report(String message, int line) {
        report(line, "", message);
    }

    public void report(String message, Token token) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    public void report(int line, String where, String message) {
        System.err.println("Error[line " + line + "]" + where + ": " + message);
        hadError = true;
    }

    public boolean hadError() {
        return hadError;
    }
}
