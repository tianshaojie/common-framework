package io.github.jsbd.common.serialization.bytebean.codec.bean;

import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.context.DecContextFactory;
import io.github.jsbd.common.serialization.bytebean.context.EncContextFactory;

/**
 * 对象的编码解码器
 * 
 */
public interface BeanFieldCodec extends ByteFieldCodec {

  /**
   * 编解码对象对应的字节长度
   * 
   * @param clazz
   * @return
   */
  int getStaticByteSize(Class<?> clazz);

  /**
   * 解码上下文
   * 
   * @return
   */
  DecContextFactory getDecContextFactory();

  /**
   * 编码上下文
   * 
   * @return
   */
  EncContextFactory getEncContextFactory();
}
