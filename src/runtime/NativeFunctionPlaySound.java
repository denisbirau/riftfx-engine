package runtime;

import error.ErrorReporter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.List;

public class NativeFunctionPlaySound implements Callable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String filename = arguments.getFirst().toString();
        new Thread(() -> {
            try {
                File audioFile = new File(filename);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                System.err.println("Could not play sound '" + filename + "': " + e.getMessage());
            }
        }).start();
        return null;
    }
}
