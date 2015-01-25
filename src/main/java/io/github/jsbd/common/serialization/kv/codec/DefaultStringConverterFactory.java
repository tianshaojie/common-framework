package io.github.jsbd.common.serialization.kv.codec;

import java.util.HashMap;
import java.util.Map;

public class DefaultStringConverterFactory implements StringConverterFactory {

  private Map<Class<?>, StringConverter> converters = new HashMap<Class<?>, StringConverter>();

  public void setConverters(Map<Class<?>, StringConverter> converters) {
    this.converters.clear();

    for (Map.Entry<Class<?>, StringConverter> entry : converters.entrySet()) {
      if (null != entry.getValue()) {
        this.converters.put(entry.getKey(), entry.getValue());
      }
    }
  }

  public DefaultStringConverterFactory setConverter(Class<?> cls, StringConverter converter) {
    this.converters.put(cls, converter);
    return this;
  }

  public StringConverter getCodecOf(Class<?> from) {
    return converters.get(from);
  }

}
