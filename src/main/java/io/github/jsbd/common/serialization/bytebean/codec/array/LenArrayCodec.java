package io.github.jsbd.common.serialization.bytebean.codec.array;

import io.github.jsbd.common.serialization.bytebean.codec.AbstractCategoryCodec;
import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecCategory;
import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Array;

public class LenArrayCodec extends AbstractCategoryCodec implements ByteFieldCodec {

  @Override
  public FieldCodecCategory getCategory() {
    return FieldCodecCategory.ARRAY;
  }

  @Override
  public DecResult decode(DecContext ctx) {
    // 默认4个字节存储数组长度
    DecResult ret = ctx.getCodecOf(int.class).decode(
        ctx.getDecContextFactory().createDecContext(ctx.getDecBytes(), int.class, ctx.getDecOwner(), null, ctx.getHeader()));
    int arrayLength = (Integer) ret.getValue();
    byte[] bytes = ret.getRemainBytes();

    Object array = null;
    if (arrayLength > 0) {
      Class<?> fieldClass = ctx.getDecClass();
      Class<?> compomentClass = fieldClass.getComponentType();

      array = Array.newInstance(compomentClass, arrayLength);
      ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

      for (int idx = 0; idx < arrayLength; idx++) {
        ret = anyCodec.decode(ctx.getDecContextFactory().createDecContext(bytes, compomentClass, ctx.getDecOwner(), null, ctx.getHeader()));
        Array.set(array, idx, ret.getValue());
        bytes = ret.getRemainBytes();
      }
    }
    return new DecResult(array, bytes);
  }

  @Override
  public byte[] encode(EncContext ctx) {
    Object array = ctx.getEncObject();
    int arrayLength = (null != array ? Array.getLength(array) : 0);
    byte[] bytes = ctx.getCodecOf(int.class).encode(ctx.getEncContextFactory().createEncContext(arrayLength, int.class, null, ctx.getHeader()));

    if (arrayLength > 0) {
      Class<?> fieldClass = ctx.getEncClass();
      Class<?> compomentClass = fieldClass.getComponentType();
      ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);
      for (int idx = 0; idx < arrayLength; idx++) {
        bytes = ArrayUtils.addAll(bytes,
            anyCodec.encode(ctx.getEncContextFactory().createEncContext(Array.get(array, idx), compomentClass, null, ctx.getHeader())));
      }
    }
    return bytes;
  }

}
