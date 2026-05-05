package com.riftfx.stdlib.ui.navigation.tab;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.riftfx.stdlib.core.AbstractCallable;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.RendererUtils;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class TabUI extends AbstractCallable {
    public TabUI() {
        super(1, 3, "title", "modifier", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, "Tab");
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 2, Callable.class, null);
        if (lambda == null) {
            throw new RuntimeException("Tab requires a content block.");
        }

        TabPane tabPane = TabPaneUI.TAB_CONTEXT.peek();
        if (tabPane == null) {
            throw new RuntimeException("Tab must be placed inside a TabPane component.");
        }

        Tab tab = new Tab(title);
        tab.setClosable(false);

        VBox tabContent = new VBox(10);
        VBox.setVgrow(tabContent, Priority.ALWAYS);
        RendererUtils.applyModifier(tabContent, "-fx-background-color: transparent;", modifierInstance);
        tab.setContent(tabContent);
        tabPane.getTabs().add(tab);

        interpreter.renderer.pushContainer(tabContent);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            interpreter.renderer.popContainer();
        }
        return null;
    }
}
