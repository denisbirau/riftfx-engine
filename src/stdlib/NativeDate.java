package stdlib;

import interpreter.Callable;
import interpreter.Interpreter;
import scanner.Token;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NativeDate implements NativeObject {
    public final LocalDate date;

    public NativeDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public Object getMember(Token member) {
        return switch (member.lexeme()) {
            case "year" -> (double) date.getYear();
            case "month" -> (double) date.getMonthValue();
            case "day" -> (double) date.getDayOfMonth();
            case "toString" -> new Callable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    return date.toString();
                }
            };
            case "format" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    String pattern = arguments.getFirst().toString();
                    return date.format(DateTimeFormatter.ofPattern(pattern));
                }
            };
            case "addDays" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof Double d) {
                        return new NativeDate(date.plusDays(d.longValue()));
                    }
                    throw new RuntimeException("addDays requires a number.");
                }
            };
            case "daysUntil" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof NativeDate other) {
                        return (double) ChronoUnit.DAYS.between(date, other.date);
                    }
                    throw new RuntimeException("daysUntil requires another Date object.");
                }
            };
            case "isBefore" -> new Callable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(List<Object> arguments, Interpreter interpreter) {
                    if (arguments.getFirst() instanceof NativeDate other) {
                        return date.isBefore(other.date);
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
