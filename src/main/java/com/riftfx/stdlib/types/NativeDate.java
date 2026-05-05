package com.riftfx.stdlib.types;

import com.riftfx.interpreter.Interpreter;
import com.riftfx.scanner.Token;
import com.riftfx.stdlib.core.AbstractCallable;
import com.riftfx.stdlib.core.NativeObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record NativeDate(LocalDate date) implements NativeObject {
    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "year" -> (double) date.getYear();
            case "month" -> (double) date.getMonthValue();
            case "day" -> (double) date.getDayOfMonth();
            case "toString" -> new AbstractCallable(0, 0) {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    return date.toString();
                }
            };
            case "format" -> new AbstractCallable(1, 1, "pattern") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    String pattern = arguments.getFirst().toString();
                    return date.format(DateTimeFormatter.ofPattern(pattern));
                }
            };
            case "addDays" -> new AbstractCallable(1, 1, "days") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof Double d) {
                        return new NativeDate(date.plusDays(d.longValue()));
                    }
                    throw new RuntimeException("addDays requires a number.");
                }
            };
            case "daysUntil" -> new AbstractCallable(1, 1, "date") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof NativeDate(LocalDate date1)) {
                        return (double) ChronoUnit.DAYS.between(date, date1);
                    }
                    throw new RuntimeException("daysUntil requires another Date object.");
                }
            };
            case "isBefore" -> new AbstractCallable(1, 1, "date") {
                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof NativeDate(LocalDate date1)) {
                        return date.isBefore(date1);
                    }
                    throw new RuntimeException("isBefore requires another Date object.");
                }
            };
            default -> throw new RuntimeException("Undefined member on Date: '" + member.lexeme() + "'.");
        };
    }

    @Override
    public void setMember(Token member, Object value) {
        throw new RuntimeException("Dates are immutable.");
    }

    @Override
    public String toString() {
        return date.toString();
    }
}
