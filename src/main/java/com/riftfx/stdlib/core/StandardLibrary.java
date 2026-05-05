package com.riftfx.stdlib.core;

import com.riftfx.stdlib.system.NativeApp;
import com.riftfx.stdlib.types.NativeDateFactory;
import com.riftfx.stdlib.io.NativeFileIO;
import com.riftfx.stdlib.math.NativeMath;
import com.riftfx.stdlib.ui.controls.*;
import com.riftfx.stdlib.ui.graphics.CanvasUI;
import com.riftfx.stdlib.ui.controls.text_inputs.PasswordFieldUI;
import com.riftfx.stdlib.ui.controls.text_inputs.TextAreaUI;
import com.riftfx.stdlib.ui.controls.text_inputs.TextFieldUI;
import com.riftfx.stdlib.ui.dialogs.ShowAlert;
import com.riftfx.stdlib.ui.dialogs.ShowFileChooser;
import com.riftfx.stdlib.ui.layout.*;
import com.riftfx.stdlib.ui.navigation.menu.MenuBarUI;
import com.riftfx.stdlib.ui.navigation.menu.MenuItemUI;
import com.riftfx.stdlib.ui.navigation.menu.MenuUI;
import com.riftfx.stdlib.ui.state.CreateState;
import com.riftfx.stdlib.ui.modifier.ModifierBase;
import com.riftfx.stdlib.ui.state.Observe;
import com.riftfx.stdlib.ui.navigation.tab.TabPaneUI;
import com.riftfx.stdlib.ui.navigation.tab.TabUI;

import java.util.Map;

import static java.util.Map.entry;

public class StandardLibrary {
    public static final Map<String, Object> GLOBALS = Map.ofEntries(
            entry("App", new NativeApp()),
            entry("Math", new NativeMath()),
            entry("Date", new NativeDateFactory()),
            entry("File", new NativeFileIO()),
            entry("Window", new WindowUI()),
            entry("Text", new TextUI()),
            entry("Column", new ColumnUI()),
            entry("Row", new RowUI()),
            entry("Button", new ButtonUI()),
            entry("State", new CreateState()),
            entry("Observe", new Observe()),
            entry("TextField", new TextFieldUI()),
            entry("Modifier", new ModifierBase()),
            entry("Checkbox", new CheckboxUI()),
            entry("Slider", new SliderUI()),
            entry("Image", new ImageUI()),
            entry("Spacer", new SpacerUI()),
            entry("PasswordField", new PasswordFieldUI()),
            entry("Stack", new StackUI()),
            entry("ProgressBar", new ProgressBarUI()),
            entry("ScrollPane", new ScrollPaneUI()),
            entry("ComboBox", new ComboBoxUI()),
            entry("TextArea", new TextAreaUI()),
            entry("Grid", new GridUI()),
            entry("GridCell", new GridCellUI()),
            entry("ListView", new ListViewUI()),
            entry("ShowAlert", new ShowAlert()),
            entry("RadioButton", new RadioButtonUI()),
            entry("DatePicker", new DatePickerUI()),
            entry("Canvas", new CanvasUI()),
            entry("Tab", new TabUI()),
            entry("TabPane", new TabPaneUI()),
            entry("TitledPane", new TitledPaneUI()),
            entry("MenuItem", new MenuItemUI()),
            entry("Menu", new MenuUI()),
            entry("MenuBar", new MenuBarUI()),
            entry("FileChooser", new ShowFileChooser()),
            entry("ProgressIndicator", new ProgressIndicatorUI()),
            entry("Spinner", new SpinnerUI()));
}
