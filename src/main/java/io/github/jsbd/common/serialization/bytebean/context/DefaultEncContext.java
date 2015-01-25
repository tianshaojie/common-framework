package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.NumberCodec;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public class DefaultEncContext extends AbstractCodecContext implements EncContext {

  private Object            encObject;
  private EncContextFactory encContextFactory;
  private XipHeader         header;

  public DefaultEncContext setCodecProvider(FieldCodecProvider codecProvider) {
    this.codecProvider = codecProvider;
    return this;
  }

  public DefaultEncContext setEncClass(Class<?> encClass) {
    this.targetType = encClass;
    return this;
  }

  public DefaultEncContext setFieldDesc(ByteFieldDesc desc) {
    this.fieldDesc = desc;
    return this;
  }

  public DefaultEncContext setNumberCodec(NumberCodec numberCodec) {
    this.numberCodec = numberCodec;
    return this;
  }

  public DefaultEncContext setEncObject(Object encObject) {
    this.encObject = encObject;
    return this;
  }

  public DefaultEncContext setEncContextFactory(EncContextFactory encContextFactory) {
    this.encContextFactory = encContextFactory;
    return this;
  }

  @Override
  public Object getEncObject() {
    return encObject;
  }

  @Override
  public Class<?> getEncClass() {
    return this.targetType;
  }

  @Override
  public EncContextFactory getEncContextFactory() {
    return encContextFactory;
  }

  public DefaultEncContext setHeader(XipHeader header) {
    this.header = header;
    return this;
  }

  public XipHeader getHeader() {
    return header;
  }

}
