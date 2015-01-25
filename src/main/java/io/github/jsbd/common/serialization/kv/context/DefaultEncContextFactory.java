package io.github.jsbd.common.serialization.kv.context;

public class DefaultEncContextFactory implements EncContextFactory {

  public EncContext createEncContext(Object encObject, Class<?> type) {
    return new DefaultEncContext().setEncClass(type).setEncObject(encObject).setEncContextFactory(this);
  }

}
