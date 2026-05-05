package com.riftfx.stdlib.ui.state;

import com.riftfx.interpreter.Interpreter;
import com.riftfx.stdlib.core.AbstractCallable;

import java.util.List;

public class CreateState extends AbstractCallable {
    public CreateState() {
        super(1, 1, "initialValue");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        int index = Interpreter.currentStateIndex++;

        Object activeValue = Interpreter.stateCache.getOrDefault(index, arguments.getFirst());

        State state = new State(activeValue);
        Interpreter.stateCache.put(index, activeValue);
        state.listeners.add(() -> {
            Interpreter.stateCache.put(index, state.value);
            return true;
        });
        return state;
    }
}
