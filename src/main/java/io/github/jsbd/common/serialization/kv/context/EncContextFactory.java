package io.github.jsbd.common.serialization.kv.context;

public interface EncContextFactory {
  EncContext createEncContext(Object encObject, Class<?> type);
}
