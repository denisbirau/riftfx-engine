package com.riftfx.interpreter;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Stack;

public class JavaFXRenderer implements UIRenderer {
    private final Stack<Pane> uiContext = new Stack<>();

    @Override
    public void pushContainer(Object container) {
        if (!(container instanceof Pane)) {
            throw new IllegalArgumentException("JavaFXRenderer expects a Pane as a container.");
        }
        uiContext.push((Pane) container);
    }

    @Override
    public void popContainer() {
        uiContext.pop();
    }

    @Override
    public void addComponent(Object component) {
        if (!(component instanceof Node)) {
            throw new IllegalArgumentException("JavaFXRenderer expects a JavaFX Node as a component.");
        }
        if (uiContext.isEmpty()) {
            throw new IllegalStateException("Cannot add component: UI Context is empty.");
        }
        uiContext.peek().getChildren().add((Node) component);
    }

    @Override
    public Object peekContainer() {
        return uiContext.isEmpty() ? null : uiContext.peek();
    }

    @Override
    public boolean isEmpty() {
        return uiContext.isEmpty();
    }
}
