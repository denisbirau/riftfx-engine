package stdlib.ui.layout;

import javafx.scene.layout.HBox;
import stdlib.ui.core.UITheme;

public class RowUI extends AbstractUIContainer<HBox> {
    @Override
    protected HBox createContainer() {
        return new HBox();
    }

    @Override
    protected void applySpacing(HBox container, double spacing) {
        container.setSpacing(spacing);
    }

    @Override
    protected double getDefaultSpacing() {
        return UITheme.SPACING_MEDIUM;
    }
}
