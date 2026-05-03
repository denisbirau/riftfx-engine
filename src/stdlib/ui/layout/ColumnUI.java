package stdlib.ui.layout;

import javafx.scene.layout.VBox;
import stdlib.ui.core.UITheme;

public class ColumnUI extends AbstractUIContainer<VBox> {
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
