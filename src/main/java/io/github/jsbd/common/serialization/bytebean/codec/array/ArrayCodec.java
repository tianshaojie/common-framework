package io.github.jsbd.common.serialization.bytebean.codec.array;

import io.github.jsbd.common.serialization.bytebean.codec.AbstractCategoryCodec;
import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecCategory;
import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Array;

public class ArrayCodec extends AbstractCategoryCodec implements ByteFieldCodec {

  @Override
  public FieldCodecCategory getCategory() {
    return FieldCodecCategory.ARRAY;
  }

  @Override
  public DecResult decode(DecContext ctx) {
    byte[] bytes = ctx.getDecBytes();
    Class<?> fieldClass = ctx.getDecClass();
    // 实际类型
    Class<?> compomentClass = fieldClass.getComponentType();
    final ByteFieldDesc desc = ctx.getFieldDesc();
    int arrayLength = 0;

    if (null == desc || !desc.hasLength()) {
      throw new RuntimeException("invalid array env.");
    } else {
      // 已经有字段记录数组长度了
      arrayLength = desc.getLength(ctx.getDecOwner());
    }

    Object array = null;
    if (arrayLength > 0) {
      array = Array.newInstance(compomentClass, arrayLength);
      ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

      for (int idx = 0; idx < arrayLength; idx++) {
        DecResult ret = anyCodec.decode(ctx.getDecContextFactory().createDecContext(bytes, compomentClass, ctx.getDecOwner(), null, ctx.getHeader()));
        Array.set(array, idx, ret.getValue());
        bytes = ret.getRemainBytes();
      }
    }

    return new DecResult(array, bytes);
  }

  @Override
  public byte[] encode(EncContext ctx) {
    Object array = ctx.getEncObject();
    Class<?> fieldClass = ctx.getEncClass();
    Class<?> compomentClass = fieldClass.getComponentType();
    int arrayLength = (null != array ? Array.getLength(array) : 0);

    ByteFieldDesc desc = ctx.getFieldDesc();
    byte[] bytes = null;

    if (null == desc || !desc.hasLength()) {
      throw new RuntimeException("invalid array env.");
    } else {
      // 已经存在字段记录数组长度，不用自动写
    }
    ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

    for (int idx = 0; idx < arrayLength; idx++) {
      bytes = ArrayUtils.addAll(bytes,
          anyCodec.encode(ctx.getEncContextFactory().createEncContext(Array.get(array, idx), compomentClass, null, ctx.getHeader())));
    }
    return bytes;
  }

}
