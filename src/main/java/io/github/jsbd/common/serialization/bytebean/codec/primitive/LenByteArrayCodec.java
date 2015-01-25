package io.github.jsbd.common.serialization.bytebean.codec.primitive;

import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LenByteArrayCodec extends AbstractPrimitiveCodec implements ByteFieldCodec {

  private static final Logger logger = LoggerFactory.getLogger(LenByteArrayCodec.class);

  @Override
  public DecResult decode(DecContext ctx) {
    //
    DecResult ret = ctx.getCodecOf(int.class).decode(
        ctx.getDecContextFactory().createDecContext(ctx.getDecBytes(), int.class, ctx.getDecOwner(), null, ctx.getHeader()));
    int arrayLength = (Integer) ret.getValue();
    byte[] bytes = ret.getRemainBytes();

    if (bytes.length < arrayLength) {
      String errmsg = "ByteArrayCodec: not enough bytes for decode, need [" + arrayLength + "], actually [" + bytes.length + "].";
      if (null != ctx.getField()) {
        errmsg += "/ cause field is [" + ctx.getField() + "]";
      }
      logger.error(errmsg);
      throw new RuntimeException(errmsg);
    }

    return new DecResult((byte[]) ArrayUtils.subarray(bytes, 0, arrayLength), ArrayUtils.subarray(bytes, arrayLength, bytes.length));
  }

  @Override
  public byte[] encode(EncContext ctx) {
    byte[] array = (byte[]) ctx.getEncObject();

    return (byte[]) ArrayUtils.addAll(
        ctx.getCodecOf(int.class).encode(
            ctx.getEncContextFactory().createEncContext((int) (null == array ? 0 : array.length), int.class, null, ctx.getHeader())), array);
  }

  @Override
  public Class<?>[] getFieldType() {
    return new Class<?>[] { byte[].class };
  }

}
