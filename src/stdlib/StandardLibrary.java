package stdlib;

import java.util.Map;

public class StandardLibrary {
    public static final Map<String, Object> GLOBALS = Map.of(
            "Math", new NativeMath(),
            "Window", new NativeUI.Window(),
            "Text", new NativeUI.Text(),
            "Column", new NativeUI.Column(),
            "Row", new NativeUI.Row(),
            "Button", new NativeUI.Button(),
            "State", new NativeUI.CreateState(),
            "Observe", new NativeUI.Observe(),
            "TextField", new NativeUI.TextField(),
            "Modifier", new NativeUI.ModifierBase()
    );
}
