package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.NumberCodec;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public class DefaultDecContextFactory implements DecContextFactory {

  private FieldCodecProvider codecProvider;
  private NumberCodec        numberCodec;

  public DecContext createDecContext(byte[] decBytes, Class<?> targetType, Object parent, ByteFieldDesc desc, XipHeader header) {
    return new DefaultDecContext().setCodecProvider(codecProvider).setDecBytes(decBytes).setDecClass(targetType).setDecOwner(parent)
        .setNumberCodec(numberCodec).setFieldDesc(desc).setDecContextFactory(this).setHeader(header);
  }

  public FieldCodecProvider getCodecProvider() {
    return codecProvider;
  }

  public void setCodecProvider(FieldCodecProvider codecProvider) {
    this.codecProvider = codecProvider;
  }

  public NumberCodec getNumberCodec() {
    return numberCodec;
  }

  public void setNumberCodec(NumberCodec numberCodec) {
    this.numberCodec = numberCodec;
  }

}
