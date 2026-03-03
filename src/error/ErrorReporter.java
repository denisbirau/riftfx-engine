package error;

public class ErrorReporter implements IErrorReporter{
    private boolean hadError = false;

    @Override
    public void report(String message, int line) {
        System.err.println("Error[line " + line + "]: " + message);
        hadError = true;
    }

    @Override
    public boolean hadError() {
        return hadError;
    }
}
