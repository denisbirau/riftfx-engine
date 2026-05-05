package stdlib.ui.layout;

import interpreter.Interpreter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import stdlib.ui.core.AbstractUIComponent;
import stdlib.ui.core.RendererUtils;

import java.util.List;

public class SpacerUI extends AbstractUIComponent {
    public SpacerUI() {
        super(0, 0);
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        RendererUtils.registerComponent(interpreter, spacer, "Spacer");
        return null;
    }
}
