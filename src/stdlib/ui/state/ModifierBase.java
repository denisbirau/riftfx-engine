package stdlib.ui.state;

import scanner.Token;
import stdlib.NativeObject;

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
