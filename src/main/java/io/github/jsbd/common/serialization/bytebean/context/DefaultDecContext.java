package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.NumberCodec;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public class DefaultDecContext extends AbstractCodecContext implements DecContext {

  private byte[]            decBytes;
  private Object            decOwner;
  private DecContextFactory decContextFactory;
  private XipHeader         header;

  public DefaultDecContext setCodecProvider(FieldCodecProvider codecProvider) {
    this.codecProvider = codecProvider;
    return this;
  }

  public DefaultDecContext setDecClass(Class<?> decClass) {
    this.targetType = decClass;
    return this;
  }

  public DefaultDecContext setDecBytes(byte[] decBytes) {
    this.decBytes = decBytes;
    return this;
  }

  public DefaultDecContext setFieldDesc(ByteFieldDesc desc) {
    this.fieldDesc = desc;
    return this;
  }

  public DefaultDecContext setDecOwner(Object decOwner) {
    this.decOwner = decOwner;
    return this;
  }

  public DefaultDecContext setNumberCodec(NumberCodec numberCodec) {
    this.numberCodec = numberCodec;
    return this;
  }

  public DefaultDecContext setDecContextFactory(DecContextFactory decContextFactory) {
    this.decContextFactory = decContextFactory;
    return this;
  }

  @Override
  public Object getDecOwner() {
    return decOwner;
  }

  @Override
  public byte[] getDecBytes() {
    return decBytes;
  }

  @Override
  public Class<?> getDecClass() {
    return this.targetType;
  }

  @Override
  public DecContextFactory getDecContextFactory() {
    return decContextFactory;
  }

  public XipHeader getHeader() {
    return header;
  }

  public DefaultDecContext setHeader(XipHeader header) {
    this.header = header;
    return this;
  }
}
