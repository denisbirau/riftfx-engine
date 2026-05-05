package com.riftfx.stdlib.ui.layout;

import javafx.scene.layout.VBox;
import com.riftfx.stdlib.ui.core.UITheme;

public class ColumnUI extends AbstractUIContainer<VBox> {
    public ColumnUI() {
        super(1, 2, "modifier", "content");
    }

    @Override
    protected VBox createContainer() {
        return new VBox();
    }

    @Override
    protected void applySpacing(VBox container, double spacing) {
        container.setSpacing(spacing);
    }

    @Override
    protected double getDefaultSpacing() {
        return UITheme.SPACING_SMALL;
    }
}
