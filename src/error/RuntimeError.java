package error;

public class RuntimeError extends RuntimeException {
    public final String message;
    public final int line;

    public RuntimeError(String message, int line) {
        this.message = message;
        this.line = line;
    }
}
