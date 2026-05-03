package stdlib.ui.state;

import scanner.Token;
import stdlib.NativeObject;

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

    @Override
    public Object getMember(Token member) {
        if (member.lexeme().equals("value")) {
            return value;
        }
        throw new RuntimeException("Undefined property: '" + member.lexeme() + "'.");
    }

    @Override
    public void setMember(Token member, Object newValue) {
        if (member.lexeme().equals("value")) {
            this.value = newValue;
            listeners.removeIf(uiListener -> !uiListener.update());
            return;
        }
        throw new RuntimeException("Undefined property: '" + member.lexeme() + "'.");
    }
}
