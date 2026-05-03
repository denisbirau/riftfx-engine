package stdlib;

import stdlib.ui.controls.*;
import stdlib.ui.dialogs.ShowAlert;
import stdlib.ui.dialogs.ShowFileChooser;
import stdlib.ui.layout.*;
import stdlib.ui.menu.MenuBarUI;
import stdlib.ui.menu.MenuItemUI;
import stdlib.ui.menu.MenuUI;
import stdlib.ui.state.CreateState;
import stdlib.ui.state.ModifierBase;
import stdlib.ui.state.Observe;

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
            entry("Spinner", new SpinnerUI())
    );
}
