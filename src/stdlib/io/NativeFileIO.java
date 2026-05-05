package stdlib.io;

import interpreter.Interpreter;
import scanner.Token;
import stdlib.core.AbstractCallable;
import stdlib.core.NativeObject;
import stdlib.types.NativeArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

public class NativeFileIO implements NativeObject {
    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "readText" -> new AbstractCallable(1, 1, "path") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    try {
                        Path path = Paths.get(arguments.getFirst().toString());
                        return Files.readString(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read file: " + e.getMessage());
                    }
                }
            };
            case "writeText" -> new AbstractCallable(2, 2, "path", "content") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    try {
                        Path path = Paths.get(arguments.get(0).toString());
                        String content = arguments.get(1).toString();
                        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        return true;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write to file: " + e.getMessage());
                    }
                }
            };
            case "exists" -> new AbstractCallable(1, 1, "path") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    Path path = Paths.get(arguments.getFirst().toString());
                    return Files.exists(path);
                }
            };
            case "delete" -> new AbstractCallable(1, 1, "path") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    try {
                        Path path = Paths.get(arguments.getFirst().toString());
                        return Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete file: " + e.getMessage());
                    }
                }
            };
            case "listDirectory" -> new AbstractCallable(1, 1, "path") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    Path path = Paths.get(arguments.getFirst().toString());
                    if (!Files.isDirectory(path)) {
                        throw new RuntimeException("Path is not a directory.");
                    }
                    try (Stream<Path> stream = Files.list(path)) {
                        List<Object> fileNames = stream.map(p -> (Object) p.getFileName().toString()).toList();
                        return new NativeArray(fileNames);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to list directory: " + e.getMessage());
                    }
                }
            };
            default -> throw new RuntimeException("Undefined member on File: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Cannot modify the global File object.");
    }
}
