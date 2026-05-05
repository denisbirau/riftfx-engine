package com.riftfx.stdlib.ui.controls.text_inputs;

import javafx.scene.control.PasswordField;

public class PasswordFieldUI extends AbstractTextInputUI<PasswordField> {
    @Override
    protected PasswordField createNode() {
        return new PasswordField();
    }
}
