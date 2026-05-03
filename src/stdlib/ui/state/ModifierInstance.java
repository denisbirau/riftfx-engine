package stdlib.ui.state;

import interpreter.Callable;
import interpreter.Interpreter;
import scanner.Token;
import stdlib.NativeObject;
import stdlib.ui.core.UITheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifierInstance implements NativeObject {
    public final Map<String, String> cssProperties;

    public ModifierInstance() {
        this.cssProperties = new HashMap<>();
    }

    public ModifierInstance(Map<String, String> cssProperties) {
        this.cssProperties = new HashMap<>(cssProperties);
    }

    private Callable createModifierFunction(String cssKey, String suffix) {
        return new Callable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(List<Object> arguments, Interpreter interpreter) {
                ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                modifierInstance.cssProperties.put(cssKey, arguments.getFirst().toString() + suffix);
                return modifierInstance;
            }
        };
    }

    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            // Sizing
            case "width" -> createModifierFunction("-fx-pref-width", "px");
            case "height" -> createModifierFunction("-fx-pref-height", "px");
            case "minWidth" -> createModifierFunction("-fx-min-width", "px");
            case "minHeight" -> createModifierFunction("-fx-min-height", "px");
            case "maxWidth" -> createModifierFunction("-fx-max-width", "px");
            case "maxHeight" -> createModifierFunction("-fx-max-height", "px");
            // Spacing & Border
            case "padding" -> createModifierFunction("-fx-padding", "px");
            case "background" -> createModifierFunction("-fx-background-color", "");
            case "borderColor" -> createModifierFunction("-fx-border-color", "");
            case "borderWidth" -> createModifierFunction("-fx-border-width", "px");
            case "alignment" -> createModifierFunction("-fx-alignment", "");
            // Typography
            case "fontSize" -> createModifierFunction("-fx-font-size", "px");
            case "fontFamily" -> createModifierFunction("-fx-font-family", "");
            case "textColor" -> createModifierFunction("-fx-text-fill", "");
            // Visual Effects
            case "opacity" -> createModifierFunction("-fx-opacity", "");
            case "rotate" -> createModifierFunction("-fx-rotate", "deg");
            case "bold" -> new Callable() {
                @Override
                public int arity() { return 0; }
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    modifierInstance.cssProperties.put("-fx-font-weight", "bold");
                    return modifierInstance;
                }
            };
            case "italic" -> new Callable() {
                @Override
                public int arity() { return 0; }
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    modifierInstance.cssProperties.put("-fx-font-style", "italic");
                    return modifierInstance;
                }
            };
            case "shadow" -> new Callable() {
                @Override
                public int arity() { return 0; }
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    modifierInstance.cssProperties.put("-fx-effect", "dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4)");
                    return modifierInstance;
                }
            };
            case "cornerRadius" -> new Callable() {
                @Override
                public int arity() { return 1; }
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    String radius = arguments.getFirst().toString() + "px";
                    modifierInstance.cssProperties.put("-fx-background-radius", radius);
                    modifierInstance.cssProperties.put("-fx-border-radius", radius);
                    return modifierInstance;
                }
            };
            case "card" -> new Callable() {
                @Override
                public int arity() { return 0; }
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    String[] props = UITheme.CARD.split(";");
                    for (String p : props) {
                        if (p.trim().isEmpty()) continue;
                        String[] kv = p.split(":");
                        if (kv.length == 2) modifierInstance.cssProperties.put(kv[0].trim(), kv[1].trim());
                    }
                    return modifierInstance;
                }
            };
            default -> throw new RuntimeException("Unknown Modifier member: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Modifiers are immutable.");
    }

    public String buildCss() {
        StringBuilder css = new StringBuilder();
        cssProperties.forEach((key, value) -> css.append(key).append(": ").append(value).append("; "));
        return css.toString();
    }
}
