package io.github.jsbd.common.serialization.kv.codec;

public interface StringConverterFactory {
  StringConverter getCodecOf(Class<?> clazz);
}
