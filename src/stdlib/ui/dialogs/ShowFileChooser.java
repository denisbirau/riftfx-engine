package stdlib.ui.dialogs;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.stage.FileChooser;
import stdlib.ui.core.InterpreterUtils;

import java.io.File;
import java.util.List;

public class ShowFileChooser implements Callable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("title");
    }

    @Override
    public boolean acceptsArity(int argCount) {
        return argCount <= arity();
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(InterpreterUtils.getArgument(arguments, 0, String.class, "Select File"));

        File selectedFile = fileChooser.showOpenDialog(null);
        return selectedFile != null ? selectedFile.getAbsolutePath() : null; // User cancelled
    }
}
