package runtime;

class Break extends RuntimeException {
    final int line;
    Break(int line) {
        this.line = line;
    }
}
