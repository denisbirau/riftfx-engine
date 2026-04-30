package stdlib;

import scanner.Token;

public interface NativeObject {
    Object getMember(Token member);
    void setMember(Token member, Object value);
}
