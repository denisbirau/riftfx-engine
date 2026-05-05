package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.control.DatePicker;
import stdlib.types.NativeDate;
import stdlib.ui.state.ReactiveBinding;
import stdlib.ui.core.*;
import stdlib.ui.modifier.ModifierInstance;
import stdlib.ui.state.State;

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
        RendererUtils.applyModifier(datePicker, UITheme.INPUT, modifierInstance);

        datePicker.valueProperty().addListener((_, _, newValue) -> {
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
