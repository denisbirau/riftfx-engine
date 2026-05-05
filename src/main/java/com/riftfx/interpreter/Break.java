package com.riftfx.interpreter;

class Break extends RuntimeException {
    final int line;

    Break(int line) {
        this.line = line;
    }
}
