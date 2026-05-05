package com.riftfx.stdlib.ui.graphics;

import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;
import javafx.scene.canvas.GraphicsContext;
import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.NativeObject;

import java.util.List;

public class NativeGraphicsContext implements NativeObject {
    private final GraphicsContext graphicsContext;

    public NativeGraphicsContext(GraphicsContext graphicsContext) {
        this.graphicsContext = graphicsContext;
    }

    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "setFill" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    graphicsContext.setFill(javafx.scene.paint.Color.web(arguments.getFirst().toString()));
                    return null;
                }
            };
            case "fillRect" -> new Callable() {
                @Override
                public int arity() {
                    return 4;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    graphicsContext.fillRect((Double) arguments.get(0), (Double) arguments.get(1),
                            (Double) arguments.get(2), (Double) arguments.get(3));
                    return null;
                }
            };
            case "fillOval" -> new Callable() {
                @Override
                public int arity() {
                    return 4;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    graphicsContext.fillOval((Double) arguments.get(0), (Double) arguments.get(1),
                            (Double) arguments.get(2), (Double) arguments.get(3));
                    return null;
                }
            };
            case "clearRect" -> new Callable() {
                @Override
                public int arity() {
                    return 4;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    graphicsContext.clearRect((Double) arguments.get(0), (Double) arguments.get(1),
                            (Double) arguments.get(2), (Double) arguments.get(3));
                    return null;
                }
            };
            default -> throw new RuntimeException("Undefined graphics method: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Graphics context properties cannot be set directly");
    }
}
