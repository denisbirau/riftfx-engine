package engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GameState {
    public static Set<String> keysPressed = new HashSet<>();
    public static List<Consumer<Graphics>> renderQueue = new ArrayList<>();
}
