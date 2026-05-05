package com.riftfx.stdlib.ui.layout;

import javafx.scene.layout.GridPane;
import com.riftfx.stdlib.ui.core.UITheme;

public class GridUI extends AbstractUIContainer<GridPane> {
    public GridUI() {
        super(1, 2, "modifier", "content");
    }

    @Override
    protected GridPane createContainer() {
        return new GridPane();
    }

    @Override
    protected void applySpacing(GridPane container, double spacing) {
        container.setHgap(spacing);
        container.setVgap(spacing);
    }

    @Override
    protected double getDefaultSpacing() {
        return UITheme.SPACING_MEDIUM;
    }
}
