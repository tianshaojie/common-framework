package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public interface EncContext extends FieldCodecContext {
  Object getEncObject();

  Class<?> getEncClass();

  EncContextFactory getEncContextFactory();

  XipHeader getHeader();
}
