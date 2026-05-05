package com.riftfx.stdlib.ui.state;

public interface UIListener {
    boolean update(); // Returns true if the update was successful, false if the UI element is dead
}
