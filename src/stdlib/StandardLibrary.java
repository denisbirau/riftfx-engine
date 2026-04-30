package stdlib;

import java.util.Map;

import static java.util.Map.entry;

public class StandardLibrary {
    public static final Map<String, Object> GLOBALS = Map.ofEntries(
            entry("Math", new NativeMath()),
            entry("Date", new NativeDateFactory()),
            entry("File", new NativeFileIO()),
            entry("Window", new NativeUI.Window()),
            entry("Text", new NativeUI.Text()),
            entry("Column", new NativeUI.Column()),
            entry("Row", new NativeUI.Row()),
            entry("Button", new NativeUI.Button()),
            entry("State", new NativeUI.CreateState()),
            entry("Observe", new NativeUI.Observe()),
            entry("TextField", new NativeUI.TextField()),
            entry("Modifier", new NativeUI.ModifierBase()),
            entry("Checkbox", new NativeUI.Checkbox()),
            entry("Slider", new NativeUI.Slider()),
            entry("Image", new NativeUI.Image()),
            entry("Spacer", new NativeUI.Spacer()),
            entry("PasswordField", new NativeUI.PasswordField()),
            entry("Stack", new NativeUI.Stack()),
            entry("ProgressBar", new NativeUI.ProgressBar()),
            entry("ScrollPane", new NativeUI.ScrollPane()),
            entry("ComboBox", new NativeUI.ComboBox()),
            entry("TextArea", new NativeUI.TextArea()),
            entry("Grid", new NativeUI.Grid()),
            entry("GridCell", new NativeUI.GridCell()),
            entry("ListView", new NativeUI.ListView()),
            entry("ShowAlert", new NativeUI.ShowAlert()),
            entry("RadioButton", new NativeUI.RadioButton()),
            entry("DatePicker", new NativeUI.DatePicker()),
            entry("Canvas", new NativeUI.CanvasUI()),
            entry("Tab", new NativeUI.TabUI()),
            entry("TabPane", new NativeUI.TabPaneUI()),
            entry("TitledPane", new NativeUI.TitledPaneUI()),
            entry("MenuItem", new NativeUI.MenuItemUI()),
            entry("Menu", new NativeUI.MenuUI()),
            entry("MenuBar", new NativeUI.MenuBarUI()),
            entry("FileChooser", new NativeUI.FileChooserUI()),
            entry("ProgressIndicator", new NativeUI.ProgressIndicatorUI())
    );
}
