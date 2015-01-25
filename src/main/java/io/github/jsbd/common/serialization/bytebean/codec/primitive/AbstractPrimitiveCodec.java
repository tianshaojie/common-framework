package io.github.jsbd.common.serialization.bytebean.codec.primitive;

import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecCategory;

public abstract class AbstractPrimitiveCodec implements ByteFieldCodec {

  @Override
  public FieldCodecCategory getCategory() {
    return null;
  }

}
