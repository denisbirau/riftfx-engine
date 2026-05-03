package stdlib.ui.dialogs;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import stdlib.ui.core.InterpreterUtils;

import java.util.List;
import java.util.Optional;

public class ShowAlert implements Callable {
    @Override
    public int arity() {
        return 3;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("type", "title", "message");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount >= 2 && argCount <= arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String typeString = InterpreterUtils.getArgument(arguments, 0, String.class, "INFORMATION").toUpperCase();
        String title = InterpreterUtils.getArgument(arguments, 1, String.class, "Alert");
        String message = InterpreterUtils.getArgument(arguments, 2, String.class, "");

        Alert.AlertType alertType = switch (typeString) {
            case "ERROR" -> Alert.AlertType.ERROR;
            case "WARNING" -> Alert.AlertType.WARNING;
            case "CONFIRMATION" -> Alert.AlertType.CONFIRMATION;
            default -> Alert.AlertType.INFORMATION;
        };

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
