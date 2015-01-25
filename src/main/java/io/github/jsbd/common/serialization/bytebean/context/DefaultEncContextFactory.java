package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.NumberCodec;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public class DefaultEncContextFactory implements EncContextFactory {

  private FieldCodecProvider codecProvider;
  private NumberCodec        numberCodec;

  public EncContext createEncContext(Object encObject, Class<?> type, ByteFieldDesc desc, XipHeader header) {
    return new DefaultEncContext().setCodecProvider(codecProvider).setEncClass(type).setEncObject(encObject).setNumberCodec(numberCodec).setFieldDesc(desc)
        .setEncContextFactory(this).setHeader(header);
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
