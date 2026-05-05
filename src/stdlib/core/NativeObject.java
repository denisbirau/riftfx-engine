package stdlib.core;

import scanner.Token;

public interface NativeObject {
    Object getMember(Token member);
    void setMember(Token member, Object value);
}
