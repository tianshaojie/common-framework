package io.github.jsbd.common.serialization.kv.context;

import io.github.jsbd.common.serialization.kv.codec.StringConverter;

public interface DecContext {
  Object getDecOwner();

  String getDecString();

  Class<?> getDecClass();

  DecContextFactory getDecContextFactory();

  StringConverter getConverterOf(Class<?> from);
}
