package com.riftfx.stdlib.core;

import com.riftfx.scanner.Token;

public interface NativeObject {
    Object getMember(Token member);

    void setMember(Token member, Object value);
}
