package io.github.jsbd.common.serialization.kv.context;

public interface DecContextFactory {
  DecContext createDecContext(String decString, Class<?> targetType, Object parent);
}
