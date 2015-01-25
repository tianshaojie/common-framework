package io.github.jsbd.common.serialization.kv.context;

public class DefaultEncContext implements EncContext {

  private Object            encObject;
  private Class<?>          targetType;
  private EncContextFactory encContextFactory;

  public DefaultEncContext setEncClass(Class<?> encClass) {
    this.targetType = encClass;
    return this;
  }

  public DefaultEncContext setEncObject(Object encObject) {
    this.encObject = encObject;
    return this;
  }

  public DefaultEncContext setEncContextFactory(EncContextFactory encContextFactory) {
    this.encContextFactory = encContextFactory;
    return this;
  }

  @Override
  public Object getEncObject() {
    return encObject;
  }

  @Override
  public Class<?> getEncClass() {
    return this.targetType;
  }

  @Override
  public EncContextFactory getEncContextFactory() {
    return encContextFactory;
  }

}
