package io.github.jsbd.common.serialization.bytebean.codec.bean;

import io.github.jsbd.common.lang.SimpleCache;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.bytebean.field.Field2Desc;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class BeanCodecUtil {
  
  private Field2Desc                                 field2Desc;
  private SimpleCache<Class<?>, List<ByteFieldDesc>> descesCache = new SimpleCache<Class<?>, List<ByteFieldDesc>>();

  public BeanCodecUtil(Field2Desc field2Desc) {
    this.field2Desc = field2Desc;
  }

  public List<ByteFieldDesc> getFieldDesces(final Class<?> clazz) {
    return descesCache.get(clazz, new Callable<List<ByteFieldDesc>>() {

      public List<ByteFieldDesc> call() {
        List<ByteFieldDesc> ret;

        Field[] fields = null;

        Class<?> itr = clazz;
        while (!itr.equals(Object.class)) {
          fields = (Field[]) ArrayUtils.addAll(itr.getDeclaredFields(), fields);
          itr = itr.getSuperclass();
        }

        ret = new ArrayList<ByteFieldDesc>(fields.length);
        for (Field field : fields) {
          ByteFieldDesc desc = field2Desc.genDesc(field);
          if (null != desc) {
            ret.add(desc);
          }
        }

        Collections.sort(ret, ByteFieldDesc.comparator);
        return ret;
      }
    });
  }
}
