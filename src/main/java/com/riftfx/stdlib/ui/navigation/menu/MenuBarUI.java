package com.riftfx.stdlib.ui.navigation.menu;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.MenuBar;
import com.riftfx.stdlib.core.InterpreterUtils;
import com.riftfx.stdlib.ui.core.*;
import com.riftfx.stdlib.ui.modifier.ModifierInstance;

import java.util.List;

public class MenuBarUI extends AbstractUIComponent {
    public static final ScopedContext<Object> MENU_CONTEXT = new ScopedContext<>();

    public MenuBarUI() {
        super(1, 2, "modifier", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 0, ModifierInstance.class, null);
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);
        if (lambda == null) {
            throw new RuntimeException("MenuBar requires content block.");
        }

        MenuBar menuBar = new MenuBar();
        RendererUtils.applyModifier(menuBar, UITheme.MENU, modifierInstance);

        MENU_CONTEXT.push(menuBar);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            MENU_CONTEXT.pop();
        }

        register(interpreter, menuBar);
        return null;
    }
}
