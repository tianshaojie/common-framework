package io.github.jsbd.common.serialization.bytebean.field;

import java.lang.reflect.Field;

/**
 * 读取给定字段的编解码描述
 */
public interface Field2Desc {
  ByteFieldDesc genDesc(Field field);
}
