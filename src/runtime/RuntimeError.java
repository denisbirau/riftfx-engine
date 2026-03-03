package runtime;

class RuntimeError extends RuntimeException {
    final String message;
    final int line;

    RuntimeError(String message, int line) {
        this.message = message;
        this.line = line;
    }
}
