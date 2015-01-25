package io.github.jsbd.common.serialization.protocol.meta;

public interface MsgCode2TypeMetainfo {
  Class<?> find(int value);
}
