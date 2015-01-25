package io.github.jsbd.common.serialization.kv.codec;

import io.github.jsbd.common.serialization.kv.context.DecContext;
import io.github.jsbd.common.serialization.kv.context.DecContextFactory;
import io.github.jsbd.common.serialization.kv.context.EncContext;
import io.github.jsbd.common.serialization.kv.context.EncContextFactory;

public interface KVCodec {

  DecContextFactory getDecContextFactory();

  Object decode(DecContext ctx);

  EncContextFactory getEncContextFactory();

  String encode(EncContext ctx);

}
