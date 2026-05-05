package com.riftfx.stdlib.ui.state;

import javafx.beans.property.Property;
import javafx.scene.Node;

import java.util.Objects;

public class ReactiveBinding {
    public static void bindStateListener(Node node, State state, Runnable onUpdate) {
        if (state == null) {
            return;
        }
        state.listeners.add(() -> {
            if (node.getParent() == null && node.getScene() == null) {
                return false;
            }
            onUpdate.run();
            return true;
        });
    }

    public static <T> void bindBidirectional(State state, Property<T> fxProperty, Node node, T defaultValue) {
        if (state == null) {
            return;
        }
        fxProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                notifyStateChanged(state, newValue);
            }
        });
        bindStateListener(node, state, () -> {
            T expected;
            try {
                @SuppressWarnings("unchecked")
                T castedValue = (T) state.value;
                expected = state.value != null ? castedValue : defaultValue;
            } catch (ClassCastException e) {
                expected = defaultValue;
            }
            if (!fxProperty.getValue().equals(expected)) {
                fxProperty.setValue(expected);
            }
        });
    }

    public static void notifyStateChanged(State state, Object newValue) {
        if (!Objects.equals(state.value, newValue)) {
            state.value = newValue;
            state.listeners.removeIf(uiListener -> !uiListener.update());
        }
    }
}
