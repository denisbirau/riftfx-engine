package com.riftfx.stdlib.ui.navigation.menu;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import com.riftfx.stdlib.core.AbstractCallable;
import com.riftfx.stdlib.core.InterpreterUtils;

import java.util.List;

public class MenuUI extends AbstractCallable {
    public MenuUI() {
        super(1, 2, "title", "content");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String title = InterpreterUtils.getArgument(arguments, 0, String.class, "Menu");
        Callable lambda = InterpreterUtils.getArgument(arguments, 1, Callable.class, null);
        if (lambda == null) {
            throw new RuntimeException("Menu requires content block.");
        }

        Menu menu = new Menu(title);

        Object parent = MenuBarUI.MENU_CONTEXT.peek();
        if (parent instanceof MenuBar menuBar) {
            menuBar.getMenus().add(menu);
        } else if (parent instanceof Menu parentMenu) {
            parentMenu.getItems().add(menu); // Supports nested sub-menus!
        } else {
            throw new RuntimeException("Menu must be placed inside a MenuBar or another Menu.");
        }

        MenuBarUI.MENU_CONTEXT.push(menu);
        try {
            lambda.call(List.of(), interpreter);
        } finally {
            MenuBarUI.MENU_CONTEXT.pop();
        }
        return menu;
    }
}
