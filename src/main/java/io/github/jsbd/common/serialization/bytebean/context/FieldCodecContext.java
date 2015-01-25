package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.NumberCodec;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;

import java.lang.reflect.Field;

/**
 * 字段编解码上下文
 */
public interface FieldCodecContext extends FieldCodecProvider {

  ByteFieldDesc getFieldDesc();

  Field getField();

  NumberCodec getNumberCodec();

  int getByteSize();

}
