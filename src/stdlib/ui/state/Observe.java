package stdlib.ui.state;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.scene.layout.VBox;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;

import java.util.List;

public class Observe implements Callable {
    @Override
    public int arity() {
        return 2;
    }

    @Override
    public List<String> parameterNames() {
        return List.of("state", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        State state = InterpreterUtils.getArgument(arguments, 0, State.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);

        if (state == null) {
            throw new RuntimeException("Observe requires a state object.");
        }
        if (lambda == null) {
            throw new RuntimeException("Observe requires a lambda content block.");
        }

        VBox container = new VBox();
        RendererUtils.registerComponent(interpreter, container, "Observe");

        UIListener recompose = () -> {
            // 1. THE LIFECYCLE CHECK
            // If an outer Observe cleared the screen, this container was orphaned.
            // Returning false tells the State object to permanently delete this listener.
            if (container.getParent() == null && container.getScene() == null) {
                return false;
            }

            // 2. THE RECOMPOSITION
            container.getChildren().clear();
            interpreter.renderer.pushContainer(container);
            try {
                lambda.call(List.of(), interpreter);
            } catch (RuntimeException e) {
                InterpreterUtils.reportError(interpreter, e, "Recomposition");
            } finally {
                interpreter.renderer.popContainer();
            }
            return true; // Still alive, keep listening!
        };

        state.listeners.add(recompose);
        recompose.update();
        return null;
    }
}
