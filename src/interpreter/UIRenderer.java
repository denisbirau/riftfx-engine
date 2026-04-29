package interpreter;

public interface UIRenderer {
    void pushContainer(Object container);
    void popContainer();
    void addComponent(Object component);
    Object peekContainer();
    boolean isEmpty();
}
