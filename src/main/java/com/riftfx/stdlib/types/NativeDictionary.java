package com.riftfx.stdlib.types;

import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.NativeObject;

import java.util.Map;

public record NativeDictionary(Map<String, Object> properties) implements NativeObject {

    @Override
    public Object getMember(Token member) {
        if (properties.containsKey(member.lexeme())) {
            return properties.get(member.lexeme());
        }
        throw new RuntimeException("Undefined property: '" + member.lexeme() + "'.");
    }

    @Override
    public void setMember(Token member, Object value) {
        properties.put(member.lexeme(), value);
    }

    @Override
    public String toString() {
        return properties.toString();
    }
}
