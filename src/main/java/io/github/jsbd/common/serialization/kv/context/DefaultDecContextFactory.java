package io.github.jsbd.common.serialization.kv.context;

import io.github.jsbd.common.serialization.kv.codec.StringConverterFactory;
import io.github.jsbd.common.serialization.kv.codec.StringConverters;

public class DefaultDecContextFactory implements DecContextFactory {

  private StringConverterFactory stringConverterFactory = StringConverters.getCommonFactory();

  public DecContext createDecContext(String decString, Class<?> targetType, Object parent) {
    return new DefaultDecContext().setStringConverterFactory(stringConverterFactory).setDecString(decString).setDecClass(targetType).setDecOwner(parent)
        .setDecContextFactory(this);
  }

  public StringConverterFactory getStringConverterFactory() {
    return stringConverterFactory;
  }

  public void setStringConverterFactory(StringConverterFactory stringConverterFactory) {
    this.stringConverterFactory = stringConverterFactory;
  }

}
