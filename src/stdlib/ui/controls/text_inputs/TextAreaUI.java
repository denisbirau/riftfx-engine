package stdlib.ui.controls.text_inputs;

import javafx.scene.control.TextArea;

public class TextAreaUI extends AbstractTextInputUI<TextArea> {
    @Override
    protected TextArea createNode() {
        return new TextArea();
    }
}
