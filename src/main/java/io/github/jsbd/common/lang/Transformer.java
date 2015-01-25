package io.github.jsbd.common.lang;

public interface Transformer<FROM, TO> {
    public TO transform(FROM from);
}
