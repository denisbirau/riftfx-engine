package com.riftfx.stdlib.ui.controls;

import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.DatePicker;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.types.NativeDate;
import com.riftfx.stdlib.ui.state.ReactiveBinding;
import com.riftfx.stdlib.ui.core.*;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;
import com.riftfx.stdlib.ui.state.State;

import java.util.List;

public class DatePickerUI extends AbstractUIComponent {
    public DatePickerUI() {
        super(1, 2, "state", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        State state = InterpreterUtils.getArgument(arguments, 0, State.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (state == null) {
            throw new RuntimeException("DatePicker requires a state object.");
        }

        DatePicker datePicker = new DatePicker();
        if (state.value instanceof NativeDate(java.time.LocalDate date)) {
            datePicker.setValue(date);
        }
        RendererUtils.applyModifier(datePicker, UITheme.DATE_PICKER, modifierInstance);

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                NativeDate newDateObject = new NativeDate(newValue);
                if (!(state.value instanceof NativeDate(java.time.LocalDate date)) || !date.equals(newValue)) {
                    ReactiveBinding.notifyStateChanged(state, newDateObject);
                }
            }
        });

        ReactiveBinding.bindStateListener(datePicker, state, () -> {
            if (state.value instanceof NativeDate(java.time.LocalDate date) && !date.equals(datePicker.getValue())) {
                datePicker.setValue(date);
            }
        });

        register(interpreter, datePicker);
        return null;
    }
}
