package io.github.jsbd.common.serialization.bytebean.field;

import io.github.jsbd.common.serialization.bytebean.annotation.ByteField;

import java.lang.reflect.Field;

public class DefaultField2Desc implements Field2Desc {

  @Override
  public ByteFieldDesc genDesc(Field field) {
    ByteField byteField = field.getAnnotation(ByteField.class);
    Class<?> clazz = field.getDeclaringClass();
    if (null != byteField) {
      try {
        DefaultFieldDesc desc = new DefaultFieldDesc().setField(field).setIndex(byteField.index()).setByteSize(byteField.bytes())
            .setCharset(byteField.charset()).setLengthField(byteField.length().equals("") ? null : clazz.getDeclaredField(byteField.length()))
            .setFixedLength(byteField.fixedLength()).setRequiredVer(byteField.requiredVer());
        return desc;
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

}
