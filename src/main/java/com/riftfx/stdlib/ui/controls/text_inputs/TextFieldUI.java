package com.riftfx.stdlib.ui.controls.text_inputs;

import com.riftfx.stdlib.ui.core.UITheme;

import javafx.scene.control.TextField;

public class TextFieldUI extends AbstractTextInputUI<TextField> {
    @Override
    protected TextField createNode() {
        return new TextField();
    }

    @Override
    protected String getBaseStyle() {
        return UITheme.TEXT_FIELD;
    }
}
