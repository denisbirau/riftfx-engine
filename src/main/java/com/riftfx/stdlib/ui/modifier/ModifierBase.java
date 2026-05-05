package com.riftfx.stdlib.ui.modifier;

import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.NativeObject;

public class ModifierBase implements NativeObject {
    @Override
    public Object getMember(Token member) {
        return new ModifierInstance().getMember(member);
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Modifiers are immutable.");
    }
}
