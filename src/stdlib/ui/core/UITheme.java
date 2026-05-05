package stdlib.ui.core;

public class UITheme {
    // 1. Breathable Spacing (Increased from 8/12/16)
    public static final double SPACING_SMALL = 12.0;
    public static final double SPACING_MEDIUM = 16.0;
    public static final double SPACING_LARGE = 24.0;

    // 2. Modern Desktop Defaults
    public static final double WINDOW_WIDTH = 1024.0;
    public static final double WINDOW_HEIGHT = 768.0;
    public static final double CANVAS_DEFAULT_SIZE = 300.0;

    // 3. Modern CSS Styling
    // Using native system fonts (San Francisco on Mac, Segoe on Windows) with a softer slate text color
    public static final String ROOT = "-fx-font-family: '-apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif'; -fx-font-size: 15px; -fx-background-color: #f8fafc;";
    public static final String TEXT = "-fx-text-fill: #1e293b;";

    // Chunky, clickable buttons with a subtle colored shadow
    public static final String BUTTON = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-cursor: hand; -fx-font-weight: 600; -fx-effect: dropshadow(three-pass-box, rgba(59, 130, 246, 0.3), 8, 0, 0, 3);";

    // Taller inputs with softer borders
    public static final String INPUT = "-fx-padding: 10px 14px; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-width: 1.5px; -fx-text-fill: #1e293b;";

    public static final String CONTAINER = "-fx-background-color: transparent;";

    // Wide, airy cards with very soft, diffused shadows
    public static final String CARD = "-fx-background-color: white; -fx-padding: 24px; -fx-background-radius: 12px; -fx-border-color: #f1f5f9; -fx-border-radius: 12px; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 20, 0, 0, 8);";

    public static final String LIST = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 4px;";
    public static final String PROGRESS = "-fx-accent: #3b82f6;";
    public static final String SLIDER = "-fx-accent: #3b82f6;";
    public static final String RADIO = "-fx-text-fill: #1e293b; -fx-spacing: 8px;";
    public static final String TAB = "-fx-background-color: transparent;";
    public static final String MENU = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-padding: 4px;";
}
