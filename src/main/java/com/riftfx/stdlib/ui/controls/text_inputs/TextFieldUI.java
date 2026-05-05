package com.riftfx.stdlib.ui.controls.text_inputs;

import javafx.scene.control.TextField;

public class TextFieldUI extends AbstractTextInputUI<TextField> {
    @Override
    protected TextField createNode() {
        return new TextField();
    }
}
