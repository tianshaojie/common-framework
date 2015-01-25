package io.github.jsbd.common.serialization.bytebean.codec.primitive;

import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteCodec extends AbstractPrimitiveCodec implements ByteFieldCodec {

  private static final Logger logger = LoggerFactory.getLogger(ByteCodec.class);

  @Override
  public Class<?>[] getFieldType() {
    return new Class<?>[] { byte.class, Byte.class };
  }

  @Override
  public DecResult decode(DecContext ctx) {
    byte[] bytes = ctx.getDecBytes();
    if (bytes.length < 1) {
      String errmsg = "ByteCodec: not enough bytes for decode, need [1], actually [" + bytes.length + "].";
      if (null != ctx.getField()) {
        errmsg += "/ cause field is [" + ctx.getField() + "]";
      }
      logger.error(errmsg);
      throw new RuntimeException(errmsg);
    }
    return new DecResult(bytes[0], ArrayUtils.subarray(bytes, 1, bytes.length));
  }

  @Override
  public byte[] encode(EncContext ctx) {
    return new byte[] { ((Byte) ctx.getEncObject()).byteValue() };
  }

}
