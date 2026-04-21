package interpreter;

import scanner.Token;

public class Return extends RuntimeException {
    final Token keyword;
    final Object value;

    Return(Token keyword, Object value) {
        this.keyword = keyword;
        this.value = value;
    }
}
