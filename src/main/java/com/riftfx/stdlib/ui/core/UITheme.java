package com.riftfx.stdlib.ui.core;

public class UITheme {
        // 1. Spacing System (Predictable rhythms)
        public static final double SPACING_SMALL = 8.0;
        public static final double SPACING_MEDIUM = 16.0;
        public static final double SPACING_LARGE = 24.0;

        // 2. Modern Desktop Defaults
        public static final double WINDOW_WIDTH = 1100.0;
        public static final double WINDOW_HEIGHT = 800.0;
        public static final double CANVAS_DEFAULT_SIZE = 300.0;

        // 3. Typography & Root
        public static final String ROOT = "-fx-font-family: 'Inter', '-apple-system', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; "
                        +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #f8fafc; " + // Clean slate-50 background
                        "-fx-font-smoothing-type: lcd;";

        public static final String TEXT = "-fx-text-fill: #0f172a;"; // Slate-900

        // 4. Buttons (Flat, dark neutral by default. No baked-in shadow to clash with
        // custom colors)
        public static final String BUTTON = "-fx-background-color: #0f172a; " +
                        "-fx-text-fill: #f8fafc; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: 500;";

        // 5. Inputs (Clean outlines, white backgrounds, no heavy inner shadows)
        private static final String BASE_INPUT = "-fx-background-color: #ffffff; " +
                        "-fx-padding: 8px 12px; " +
                        "-fx-border-color: #cbd5e1; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-width: 1px; " +
                        "-fx-text-fill: #0f172a; " +
                        "-fx-prompt-text-fill: #94a3b8;";

        public static final String TEXT_FIELD = BASE_INPUT;
        public static final String TEXT_AREA = BASE_INPUT;
        public static final String PASSWORD_FIELD = BASE_INPUT;
        public static final String COMBO_BOX = BASE_INPUT;
        public static final String DATE_PICKER = BASE_INPUT;
        public static final String SPINNER = BASE_INPUT;

        // 6. Transparent Containers
        public static final String CONTAINER = "-fx-background-color: transparent;";
        public static final String TRANSPARENT_BG = "-fx-background-color: transparent;";

        public static final String IMAGE_CONTAINER = TRANSPARENT_BG;
        public static final String GRID_CELL = TRANSPARENT_BG;
        public static final String SCROLL_PANE = TRANSPARENT_BG + " -fx-background: transparent;";
        public static final String SPACER = TRANSPARENT_BG;
        public static final String TAB_CONTENT = TRANSPARENT_BG;

        // 7. Complex Components (Cards, Lists)
        public static final String CARD = "-fx-background-color: #ffffff; " +
                        "-fx-padding: 24px; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-border-width: 1px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.04), 12, 0, 0, 4);"; // Highly neutral
                                                                                                     // shadow

        public static final String LIST = "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 4px;";

        // 8. Accent Elements
        public static final String PROGRESS = "-fx-accent: #0f172a;";
        public static final String SLIDER = "-fx-accent: #0f172a;";
        public static final String RADIO = "-fx-text-fill: #0f172a; -fx-spacing: 8px; -fx-accent: #0f172a;";
        public static final String TAB = TRANSPARENT_BG;

        public static final String MENU = "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-padding: 4px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.06), 10, 0, 0, 4);";
}