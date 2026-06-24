package com.riftfx.stdlib.ui.controls.text_inputs;

import com.riftfx.stdlib.ui.core.UITheme;

import javafx.scene.control.TextArea;

public class TextAreaUI extends AbstractTextInputUI<TextArea> {
    @Override
    protected TextArea createNode() {
        return new TextArea();
    }

    @Override
    protected String getBaseStyle() {
        return UITheme.TEXT_AREA;
    }
}
