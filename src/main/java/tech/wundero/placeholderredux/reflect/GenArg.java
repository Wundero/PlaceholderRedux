package tech.wundero.placeholderredux.reflect;

import tech.wundero.placeholderredux.api.function.FunctionArg;

import java.util.Optional;

public class GenArg {

    private FunctionArg<?> fa;
    private Integer pos;
    private boolean nullable;

    GenArg(FunctionArg<?> fa, Integer pos, boolean nullable) {
        this.fa = fa;
        this.pos = pos;
        this.nullable = nullable && !fa.type().isPrimitive();
    }

    public FunctionArg<?> getFA() {
        return fa;
    }

    public Class<?> getClazz() {
        return fa.type();
    }

    public Integer getPos() {
        return pos;
    }

    public boolean isNullable() {
        return nullable;
    }
}
