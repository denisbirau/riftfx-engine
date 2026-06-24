package com.riftfx.stdlib.ui.controls.text_inputs;

import com.riftfx.stdlib.ui.core.UITheme;

import javafx.scene.control.PasswordField;

public class PasswordFieldUI extends AbstractTextInputUI<PasswordField> {
    @Override
    protected PasswordField createNode() {
        return new PasswordField();
    }

    @Override
    protected String getBaseStyle() {
        return UITheme.PASSWORD_FIELD;
    }
}
