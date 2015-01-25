package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.ByteBeanUtil;
import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecCategory;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.NumberCodec;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;

import java.lang.reflect.Field;

public class AbstractCodecContext extends ByteBeanUtil implements FieldCodecContext {

  protected FieldCodecProvider codecProvider = null;
  protected ByteFieldDesc      fieldDesc;
  protected NumberCodec        numberCodec;
  protected Class<?>           targetType;

  @Override
  public ByteFieldDesc getFieldDesc() {
    return fieldDesc;
  }

  @Override
  public Field getField() {
    if (null != this.fieldDesc) {
      return this.fieldDesc.getField();
    } else {
      return null;
    }
  }

  @Override
  public NumberCodec getNumberCodec() {
    /*
     * if (null != fieldDesc) { // TODO 字节序暂默认使用小端 return
     * DefaultNumberCodecs.getLittleEndianNumberCodec(); }
     */
    return numberCodec;
  }

  @Override
  public int getByteSize() {
    int ret = -1;
    if (null != fieldDesc) {
      ret = fieldDesc.getByteSize();
    } else if (null != targetType) {
      ret = super.type2DefaultByteSize(targetType);
    }
    return ret;
  }

  public ByteFieldCodec getCodecOf(FieldCodecCategory type) {
    if (null != codecProvider) {
      return codecProvider.getCodecOf(type);
    } else {
      return null;
    }
  }

  public ByteFieldCodec getCodecOf(Class<?> clazz) {
    if (null != codecProvider) {
      return codecProvider.getCodecOf(clazz);
    } else {
      return null;
    }
  }

}
