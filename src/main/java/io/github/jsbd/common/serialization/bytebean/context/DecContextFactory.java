package io.github.jsbd.common.serialization.bytebean.context;

import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;

public interface DecContextFactory {

  DecContext createDecContext(byte[] decBytes, Class<?> targetType, Object parent, ByteFieldDesc desc, XipHeader header);

}
