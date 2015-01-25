package io.github.jsbd.common.serialization.kv;

import io.github.jsbd.common.lang.FieldUtil;
import io.github.jsbd.common.lang.SimpleCache;
import io.github.jsbd.common.serialization.kv.annotation.KeyValueAttribute;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class KVUtils {

  private static SimpleCache<Class<?>, Field[]> kvFieldsCache = new SimpleCache<Class<?>, Field[]>();

  public static Field[] getKVFieldsOf(final Class<?> kvType) {
    return kvFieldsCache.get(kvType, new Callable<Field[]>() {

      public Field[] call() throws Exception {
        return FieldUtil.getAnnotationFieldsOf(kvType, KeyValueAttribute.class);
      }

    });
  }

}
