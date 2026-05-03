package stdlib.ui.controls;

import interpreter.Interpreter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import stdlib.ui.core.InterpreterUtils;
import stdlib.ui.core.RendererUtils;
import stdlib.ui.state.ModifierInstance;

import java.util.List;

public class ImageUI extends AbstractUIComponent {
    public ImageUI() {
        super(1, 2, "url", "modifier");
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        String url = InterpreterUtils.getArgument(arguments, 0, String.class, null);
        ModifierInstance modifierInstance = InterpreterUtils.getArgument(arguments, 1, ModifierInstance.class, null);
        if (url == null) {
            throw new RuntimeException("Image requires an URL or file path.");
        }

        ImageView imageView = new ImageView(new Image(url));
        imageView.setPreserveRatio(true);
        RendererUtils.applyModifier(imageView, "", modifierInstance);

        register(interpreter, imageView);
        return null;
    }
}
