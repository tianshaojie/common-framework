package io.github.jsbd.common.serialization.kv.context;

import io.github.jsbd.common.serialization.kv.codec.StringConverter;
import io.github.jsbd.common.serialization.kv.codec.StringConverterFactory;

public class DefaultDecContext implements DecContext {

  private String                 decString;
  private Object                 decOwner;
  private Class<?>               targetType;

  private StringConverterFactory stringConverterFactory;

  private DecContextFactory      decContextFactory;

  public DefaultDecContext setDecClass(Class<?> decClass) {
    this.targetType = decClass;
    return this;
  }

  public DefaultDecContext setDecString(String decString) {
    this.decString = decString;
    return this;
  }

  public DefaultDecContext setDecOwner(Object decOwner) {
    this.decOwner = decOwner;
    return this;
  }

  public DefaultDecContext setStringConverterFactory(StringConverterFactory stringConverterFactory) {
    this.stringConverterFactory = stringConverterFactory;
    return this;
  }

  public DefaultDecContext setDecContextFactory(DecContextFactory decContextFactory) {
    this.decContextFactory = decContextFactory;
    return this;
  }

  @Override
  public Object getDecOwner() {
    return decOwner;
  }

  @Override
  public String getDecString() {
    return decString;
  }

  @Override
  public Class<?> getDecClass() {
    return this.targetType;
  }

  @Override
  public DecContextFactory getDecContextFactory() {
    return decContextFactory;
  }

  public StringConverter getConverterOf(Class<?> from) {
    return stringConverterFactory.getCodecOf(from);
  }

}
