package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public interface DecContext extends FieldCodecContext {

  Object getDecOwner();

  byte[] getDecBytes();

  Class<?> getDecClass();

  XipHeader getHeader();

  DecContextFactory getDecContextFactory();

}
