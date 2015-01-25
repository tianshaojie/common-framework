package io.github.jsbd.common.serialization.kv.context;

public interface EncContext {
  
  Object getEncObject();

  Class<?> getEncClass();

  EncContextFactory getEncContextFactory();
  
}
