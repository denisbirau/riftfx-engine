package com.riftfx.stdlib.ui.modifier;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.AbstractCallable;
import com.riftfx.stdlib.core.NativeObject;
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

    private Callable createFlagModifier(String cssKey, String cssValue) {
        return new Callable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(List<Object> arguments, Interpreter interpreter) {
                ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                modifierInstance.cssProperties.put(cssKey, cssValue);
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
            case "size" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    String size = arguments.getFirst().toString() + "px";
                    modifierInstance.cssProperties.put("-fx-pref-width", size);
                    modifierInstance.cssProperties.put("-fx-pref-height", size);
                    return modifierInstance;
                }
            };

            // Spacing & Borders
            case "padding" -> createModifierFunction("-fx-padding", "px");
            case "background" -> createModifierFunction("-fx-background-color", "");
            case "borderColor" -> createModifierFunction("-fx-border-color", "");
            case "borderWidth" -> createModifierFunction("-fx-border-width", "px");
            case "alignment" -> createModifierFunction("-fx-alignment", "");

            // Typography
            case "fontSize" -> createModifierFunction("-fx-font-size", "px");
            case "fontFamily" -> createModifierFunction("-fx-font-family", "");
            case "textColor" -> createModifierFunction("-fx-text-fill", "");

            // Visual Effects and Flags
            case "opacity" -> createModifierFunction("-fx-opacity", "");
            case "rotate" -> createModifierFunction("-fx-rotate", "deg");
            case "bold" -> createFlagModifier("-fx-font-weight", "bold");
            case "italic" -> createFlagModifier("-fx-font-style", "italic");

            // Shadows: Now dynamically accepts a custom color, or defaults to a neutral
            // dark tint
            case "shadow" -> new AbstractCallable(0, 1, "color") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    String shadowCss = "dropshadow(three-pass-box, rgba(0,0,0,0.15), 14, 0, 0, 6)";
                    if (!arguments.isEmpty()) {
                        shadowCss = "dropshadow(three-pass-box, " + arguments.getFirst().toString() + ", 14, 0, 0, 6)";
                    }
                    modifierInstance.cssProperties.put("-fx-effect", shadowCss);
                    return modifierInstance;
                }
            };
            case "clearShadow" -> createFlagModifier("-fx-effect", "none");

            // Utility Bundles
            case "card" -> new Callable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    modifierInstance.cssProperties.put("-fx-background-color", "#ffffff");
                    modifierInstance.cssProperties.put("-fx-padding", "24px");
                    modifierInstance.cssProperties.put("-fx-background-radius", "12px");
                    modifierInstance.cssProperties.put("-fx-border-color", "#e2e8f0");
                    modifierInstance.cssProperties.put("-fx-border-radius", "12px");
                    modifierInstance.cssProperties.put("-fx-border-width", "1px");
                    modifierInstance.cssProperties.put("-fx-effect",
                            "dropshadow(three-pass-box, rgba(0, 0, 0, 0.04), 12, 0, 0, 4)");
                    return modifierInstance;
                }
            };

            case "cornerRadius" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    ModifierInstance modifierInstance = new ModifierInstance(cssProperties);
                    String radius = arguments.getFirst().toString() + "px";
                    modifierInstance.cssProperties.put("-fx-background-radius", radius);
                    modifierInstance.cssProperties.put("-fx-border-radius", radius);
                    return modifierInstance;
                }
            };
            default -> throw new RuntimeException("Unknown Modifier property: '" + member.lexeme() + "'.");
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