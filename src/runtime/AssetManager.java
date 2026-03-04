package runtime;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image getImage(String path) {
        if (!cache.containsKey(path)) {
            Image image = new ImageIcon(path).getImage();
            cache.put(path, image);
        }
        return cache.get(path);
    }
}
