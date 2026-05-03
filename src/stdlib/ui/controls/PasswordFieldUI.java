package stdlib.ui.controls;

import javafx.scene.control.PasswordField;

public class PasswordFieldUI extends AbstractTextInputUI<PasswordField> {
    @Override
    protected PasswordField createNode() {
        return new PasswordField();
    }
}
