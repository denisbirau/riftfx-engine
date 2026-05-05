package com.riftfx.stdlib.core;

import com.riftfx.error.RuntimeError;
import com.riftfx.interpreter.Callable;
import com.riftfx.interpreter.Interpreter;

import java.util.List;

public class InterpreterUtils {
    public static <T> T getArgument(List<Object> arguments, int index, Class<T> tClass, T defaultValue) {
        if (index >= arguments.size() || arguments.get(index) == null) {
            return defaultValue;
        }
        Object argument = arguments.get(index);
        if (!tClass.isInstance(argument)) {
            throw new RuntimeException(
                    "Argument at position " + (index + 1) + " must be of type " + tClass.getSimpleName() + ".");
        }
        return tClass.cast(argument);
    }

    public static void reportError(Interpreter interpreter, RuntimeException e, String context) {
        if (e instanceof RuntimeError runtimeError) {
            interpreter.errorReporter.report(context + " Error: " + runtimeError.getMessage(), runtimeError.getToken());
        } else {
            System.err.println("Fatal " + context + " Error: " + e.getMessage());
        }
    }

    public static void executeSafe(Interpreter interpreter, Callable lambda, List<Object> arguments, String context) {
        try {
            lambda.call(arguments, interpreter);
        } catch (RuntimeException e) {
            reportError(interpreter, e, context);
        }
    }
}
