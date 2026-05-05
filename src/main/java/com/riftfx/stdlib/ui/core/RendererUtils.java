package com.riftfx.stdlib.ui.core;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.Node;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

public class RendererUtils {
    public static void registerComponent(Interpreter interpreter, Node node, String name) {
        if (interpreter.renderer.isEmpty()) {
            throw new RuntimeException(name + " must be called inside an UI container.");
        }
        interpreter.renderer.addComponent(node);
    }

    public static void applyModifier(Node node, String baseStyle, ModifierInstance modifierInstance) {
        String finalStyle = baseStyle;
        if (modifierInstance != null) {
            finalStyle += " " + modifierInstance.buildCss();
        }
        node.setStyle(finalStyle);
    }
}
