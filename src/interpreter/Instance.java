package interpreter;

import scanner.Token;
import error.RuntimeError;

import java.util.HashMap;
import java.util.Map;

class Instance {
    private final Class myClass;
    private final Map<String, Object> members = new HashMap<>();

    Instance(Class myClass) {
        this.myClass = myClass;
    }

    Object get(Token member) {
        if (members.containsKey(member.lexeme())) {
            return members.get(member.lexeme());
        }

        var method = myClass.getMethod(member.lexeme());
        if (method != null) {
            return method.bindInstance(this);
        }

        throw new RuntimeError("Undefined member: '" + member.lexeme() + "'.", member);
    }

    void set(Token property, Object newValue) {
        members.put(property.lexeme(), newValue);
    }

    @Override
    public String toString() {
        return myClass + " instance";
    }
}
