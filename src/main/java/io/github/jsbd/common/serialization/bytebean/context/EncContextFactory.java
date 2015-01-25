package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public interface EncContextFactory {
  EncContext createEncContext(Object encObject, Class<?> type, ByteFieldDesc desc, XipHeader header);
}
