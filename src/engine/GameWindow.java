package engine;

import runtime.Interpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class GameWindow extends JPanel {
    private final Interpreter interpreter;

    public GameWindow(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void start() {
        JFrame frame = new JFrame("Bachelor Thesis");
        frame.add(this);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                GameState.keysPressed.add(KeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                GameState.keysPressed.remove(KeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
            }
        });

        frame.setVisible(true);

        final long[] lastTime = { System.currentTimeMillis() };
        Timer timer = new Timer(16, _ -> {
            long now = System.currentTimeMillis();
            double dt = (now - lastTime[0]) / 1000.0;
            lastTime[0] = now;

            GameState.renderQueue.clear();
            interpreter.callScriptFunction("update", List.of(dt));
            interpreter.callScriptFunction("draw");
            this.repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        for (Consumer<Graphics> command : GameState.renderQueue) {
            command.accept(g);
        }
    }
}
