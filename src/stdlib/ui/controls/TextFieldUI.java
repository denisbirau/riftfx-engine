package stdlib.ui.controls;

import javafx.scene.control.TextField;

public class TextFieldUI extends AbstractTextInputUI<TextField> {
    @Override
    protected TextField createNode() {
        return new TextField();
    }
}
