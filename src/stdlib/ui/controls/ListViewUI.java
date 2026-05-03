package stdlib.ui.controls;

import interpreter.Callable;
import interpreter.Interpreter;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import stdlib.NativeArray;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.ReactiveBinding;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.core.UITheme;
import stdlib.ui.state.ModifierInstance;
import stdlib.ui.state.State;

import java.util.ArrayList;
import java.util.List;

public class ListViewUI extends AbstractUIComponent {
    public ListViewUI() {
        super(2, 3, "items", "modifier", "itemBuilder");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        Object itemsArgument = arguments.isEmpty() ? null : arguments.getFirst();
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);
        if (lambda == null) {
            throw new RuntimeException("ListView requires an itemBuilder content block.");
        }

        State stateArgument = itemsArgument instanceof State s ? s : null;

        NativeArray sourceArray = stateArgument != null ? (NativeArray) stateArgument.value : (itemsArgument instanceof NativeArray na ? na : null);
        if (sourceArray == null) {
            throw new RuntimeException("ListView requires a NativeArray or a State containing a NativeArray.");
        }
        List<Object> itemsList = new ArrayList<>(sourceArray.elements());

        ListView<Object> listView = new ListView<>();
        listView.getItems().addAll(itemsList);
        RendererUtils.applyModifier(listView, UITheme.LIST, modifierInstance);

        listView.setCellFactory(_ -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    StackPane cellContainer = new StackPane();
                    StackPane.setAlignment(cellContainer, Pos.CENTER_LEFT);

                    interpreter.renderer.pushContainer(cellContainer);
                    try {
                        lambda.call(List.of(item), interpreter);
                        setGraphic(cellContainer);
                    } catch (RuntimeException e) {
                        InterpreterUtils.reportError(interpreter, e, "ListView cell rendering");
                    } finally {
                        interpreter.renderer.popContainer();
                    }
                }
            }
        });

        if (stateArgument != null) {
            ReactiveBinding.bindStateListener(listView, stateArgument, () -> {
                List<Object> newItems = new ArrayList<>();
                if (stateArgument.value instanceof NativeArray(List<Object> elements)) {
                    newItems.addAll(elements);
                }
                listView.getItems().setAll(newItems);
            });
        }

        register(interpreter, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return null;
    }
}
