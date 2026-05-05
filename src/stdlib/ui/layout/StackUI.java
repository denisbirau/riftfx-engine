package stdlib.ui.layout;

import javafx.scene.layout.StackPane;

public class StackUI extends AbstractUIContainer<StackPane> {
    public StackUI() {
        super(1, 2, "modifier", "content");
    }

    @Override
    protected StackPane createContainer() {
        return new StackPane();
    }

    @Override
    protected void applySpacing(StackPane container, double spacing) {

    }

    @Override
    protected double getDefaultSpacing() {
        return 0.0;
    }
}
