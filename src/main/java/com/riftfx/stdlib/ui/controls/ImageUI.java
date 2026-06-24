package com.riftfx.stdlib.ui.controls;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import com.riftfx.stdlib.ui.core.AbstractUIComponent;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.RendererUtils;
import com.riftfx.stdlib.ui.core.UITheme;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class ImageUI extends AbstractUIComponent {
    public ImageUI() {
        super(1, 2, "url", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String url = InterpreterUtils.getArgument(arguments, 0, String.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (url == null) {
            throw new RuntimeException("Image requires an URL or file path.");
        }

        ImageView imageView = new ImageView(new Image(url));
        imageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(imageView);
        if (modifierInstance != null) {
            if (modifierInstance.cssProperties.containsKey("-fx-pref-width")) {
                imageView.setFitWidth(parsePx(modifierInstance.cssProperties.get("-fx-pref-width")));
            }
            if (modifierInstance.cssProperties.containsKey("-fx-pref-height")) {
                imageView.setFitHeight(parsePx(modifierInstance.cssProperties.get("-fx-pref-height")));
            }
            if (modifierInstance.cssProperties.containsKey("-fx-background-radius")) {
                double radius = parsePx(modifierInstance.cssProperties.get("-fx-background-radius"));
                Rectangle clip = new Rectangle();
                clip.setArcWidth(radius * 2);
                clip.setArcHeight(radius * 2);

                clip.setWidth(imageView.getBoundsInLocal().getWidth());
                clip.setHeight(imageView.getBoundsInLocal().getHeight());

                imageView.boundsInLocalProperty().addListener((observable, oldBounds, newBounds) -> {
                    clip.setWidth(newBounds.getWidth());
                    clip.setHeight(newBounds.getHeight());
                });
                imageView.setClip(clip);
            }
            RendererUtils.applyModifier(imageContainer, UITheme.IMAGE_CONTAINER, modifierInstance);
        }

        register(interpreter, imageContainer);
        return null;
    }

    private double parsePx(String cssValue) {
        try {
            return Double.parseDouble(cssValue.replace("px", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
