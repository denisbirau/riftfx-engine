package stdlib.ui.dialogs;

import interpreter.Interpreter;
import javafx.stage.FileChooser;
import stdlib.core.AbstractCallable;
import stdlib.ui.core.InterpreterUtils;

import java.io.File;
import java.util.List;

public class ShowFileChooser extends AbstractCallable {
    public ShowFileChooser() {
        super(1, 1, "title");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(InterpreterUtils.getArgument(arguments, 0, String.class, "Select File"));

        File selectedFile = fileChooser.showOpenDialog(null);
        return selectedFile != null ? selectedFile.getAbsolutePath() : null; // User cancelled
    }
}
