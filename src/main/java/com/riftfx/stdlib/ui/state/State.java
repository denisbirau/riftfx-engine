package com.riftfx.stdlib.ui.state;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.NativeObject;

import java.util.ArrayList;
import java.util.List;

public class State implements NativeObject {
    public Object value;
    public final List<UIListener> listeners = new ArrayList<>();

    public State(Object value) {
        this.value = value;
    }

    public <T> T getValueOrDefault(Class<T> type, T fallback) {
        if (type.isInstance(this.value)) {
            return type.cast(this.value);
        }
        return fallback;
    }

    private void triggerListeners() {
        List<UIListener> deadListeners = new ArrayList<>();

        for (UIListener listener : new ArrayList<>(listeners)) {
            if (!listener.update()) {
                deadListeners.add(listener);
            }
        }

        listeners.removeAll(deadListeners);
    }

    @Override
    public Object getMember(Token member) {
        if (member.lexeme().equals("value")) {
            return value;
        } else if (member.lexeme().equals("notify")) {
            return new Callable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    triggerListeners();
                    return null;
                }
            };
        }
        throw new RuntimeException("Undefined property: '" + member.lexeme() + "'.");
    }

    @Override
    public void setMember(Token member, Object newValue) {
        if (member.lexeme().equals("value")) {
            this.value = newValue;
            triggerListeners();
            return;
        }
        throw new RuntimeException("Undefined property: '" + member.lexeme() + "'.");
    }
}
