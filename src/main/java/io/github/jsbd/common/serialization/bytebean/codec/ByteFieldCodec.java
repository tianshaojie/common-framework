package io.github.jsbd.common.serialization.bytebean.codec;

import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;

public interface ByteFieldCodec {

  FieldCodecCategory getCategory();

  Class<?>[] getFieldType();

  DecResult decode(DecContext ctx);

  byte[] encode(EncContext ctx);

}
