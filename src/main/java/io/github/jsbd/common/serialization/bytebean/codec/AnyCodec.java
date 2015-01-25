package io.github.jsbd.common.serialization.bytebean.codec;

import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnyCodec implements ByteFieldCodec {

  private static final Logger logger = LoggerFactory.getLogger(AnyCodec.class);

  @Override
  public FieldCodecCategory getCategory() {
    return FieldCodecCategory.ANY;
  }

  @Override
  public Class<?>[] getFieldType() {
    return null;
  }

  @Override
  public DecResult decode(DecContext ctx) {
    Class<?> clazz = ctx.getDecClass();

    ByteFieldCodec codec = ctx.getCodecOf(clazz);
    if (null == codec) {
      if (clazz.isArray()) {
        codec = ctx.getCodecOf(FieldCodecCategory.ARRAY);
      } else {
        codec = ctx.getCodecOf(FieldCodecCategory.BEAN);
      }
    }

    if (null != codec) {
      return codec.decode(ctx);
    } else {
      logger.error("decode : can not find matched codec for field [" + ctx.getField() + "].");
    }
    return new DecResult(null, ctx.getDecBytes());
  }

  @Override
  public byte[] encode(EncContext ctx) {
    Class<?> clazz = ctx.getEncClass();

    ByteFieldCodec codec = ctx.getCodecOf(clazz);
    XipHeader header = ctx.getHeader();
    if (header != null && ctx.getFieldDesc() != null) {
      if (header.getBasicVer() < ctx.getFieldDesc().getRequiredVer()) {
        return null;
      }
    }
    if (null == codec) {
      if (clazz.isArray()) {
        codec = ctx.getCodecOf(FieldCodecCategory.ARRAY);
      } else {
        codec = ctx.getCodecOf(FieldCodecCategory.BEAN);
      }
    }

    if (null != codec) {
      return codec.encode(ctx);
    } else {
      logger.error("encode : can not find matched codec for field [" + ctx.getField() + "].");
    }

    return new byte[0];
  }

}
