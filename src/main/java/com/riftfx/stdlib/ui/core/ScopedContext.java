package com.riftfx.stdlib.ui.core;

import java.util.Stack;

public class ScopedContext<T> {
    private final ThreadLocal<Stack<T>> stackThreadLocal = ThreadLocal.withInitial(Stack::new);

    public void push(T item) {
        stackThreadLocal.get().push(item);
    }

    public void pop() {
        stackThreadLocal.get().pop();
    }

    public T peek() {
        Stack<T> stack = stackThreadLocal.get();
        return stack.isEmpty() ? null : stack.peek();
    }
}
