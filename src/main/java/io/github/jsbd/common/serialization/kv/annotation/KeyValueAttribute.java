package io.github.jsbd.common.serialization.kv.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValueAttribute {

  public abstract String key() default "";

  public abstract String desc() default "";

}
