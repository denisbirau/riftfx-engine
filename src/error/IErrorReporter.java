package error;

public interface IErrorReporter {
    void report(String message, int line);
    boolean hadError();
}
