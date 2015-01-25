package io.github.jsbd.common.serialization.bytebean.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义协议字段
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ByteField {

  /**
   * 消息体中的索引位置
   *
   * @return
   */
  int index();

  /**
   * 在消息体中的字节长度，为-1时，取字段类型的长度
   *
   * @return
   */
  int bytes() default -1;

  /**
   * 定义字段类型的长度字
   *
   * @return
   */
  String length() default "";

  /**
   * 定义字段类型的字符集
   *
   * @return
   */
  String charset() default "UTF-16";

  /**
   * 定义字段的为定长字节
   *
   * @return
   */
  int fixedLength() default -1;

  /**
   * 定义字段描述
   *
   * @return
   */
  String description() default "";

  /**
   * 与head头版本匹配
   *
   * @return
   */
  int requiredVer() default 1;
}
