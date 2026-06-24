package com.riftfx.stdlib.ui.core;

import java.util.LinkedHashMap;
import java.util.Map;

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
        // Parse the base style string into a mutable map
        Map<String, String> mergedStyles = parseCss(baseStyle);

        // Apply modifier overrides directly on top of the base rules
        if (modifierInstance != null) {
            mergedStyles.putAll(modifierInstance.cssProperties);
        }

        // Re-serialize back to a clean CSS string
        StringBuilder finalCss = new StringBuilder();
        mergedStyles.forEach((key, value) -> finalCss.append(key).append(": ").append(value).append("; "));
        node.setStyle(finalCss.toString());
    }

    private static Map<String, String> parseCss(String css) {
        Map<String, String> map = new LinkedHashMap<>();
        if (css == null || css.isBlank())
            return map;

        String[] rules = css.split(";");
        for (String rule : rules) {
            if (rule.trim().isEmpty())
                continue;
            // Split strictly into 2 parts to safely handle values containing colons (like
            // URLs or data URIs)
            String[] kv = rule.split(":", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }
}
