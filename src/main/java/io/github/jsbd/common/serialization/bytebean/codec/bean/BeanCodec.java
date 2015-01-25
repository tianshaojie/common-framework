package io.github.jsbd.common.serialization.bytebean.codec.bean;

import io.github.jsbd.common.serialization.bytebean.codec.AbstractCategoryCodec;
import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecCategory;
import io.github.jsbd.common.serialization.bytebean.context.*;
import io.github.jsbd.common.serialization.bytebean.field.ByteFieldDesc;
import io.github.jsbd.common.serialization.bytebean.field.Field2Desc;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

public class BeanCodec extends AbstractCategoryCodec implements BeanFieldCodec {

  private static final Logger logger = LoggerFactory.getLogger(BeanCodec.class);

  private DecContextFactory   decContextFactory;
  private EncContextFactory   encContextFactory;
  private BeanCodecUtil       util;

  public BeanCodec(Field2Desc field2Desc) {
    this.util = new BeanCodecUtil(field2Desc);
  }

  @Override
  public DecResult decode(DecContext ctx) {
    byte[] bytes = ctx.getDecBytes();
    Class<?> clazz = ctx.getDecClass();

    Object target = null;

    try {
      target = clazz.newInstance();

      List<ByteFieldDesc> desces = util.getFieldDesces(clazz);
      ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);
      XipHeader header = ctx.getHeader();
      for (ByteFieldDesc desc : desces) {

        if (header != null) {
          if (header.getBasicVer() < desc.getRequiredVer()) {
            continue;
          }
        }
        Field field = desc.getField();

        Class<?> fieldClass = field.getType();

        DecResult ret = anyCodec.decode(decContextFactory.createDecContext(bytes, fieldClass, target, desc, ctx.getHeader()));
        Object fieldValue = ret.getValue();
        bytes = ret.getRemainBytes();

        field.setAccessible(true);
        field.set(target, fieldValue);
      }

    } catch (InstantiationException e) {
      logger.error("BeanCodec:", e);
    } catch (IllegalAccessException e) {
      logger.error("BeanCodec:", e);
    }

    return new DecResult(target, bytes);
  }

  @Override
  public byte[] encode(EncContext ctx) {
    Object bean = ctx.getEncObject();
    if (null == bean) {
      String errmsg = "BeanCodec: bean is null";
      if (null != ctx.getField()) {
        errmsg += "/ cause field is [" + ctx.getField() + "]";
      } else {
        errmsg += "/ cause type is [" + ctx.getEncClass() + "]";
      }
      logger.error(errmsg);
      return new byte[0];
    }
    List<ByteFieldDesc> desces = util.getFieldDesces(bean.getClass());
    byte[] ret = new byte[0];
    ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);
    XipHeader header = ctx.getHeader();
    for (ByteFieldDesc desc : desces) {

      if (header != null) {
        if (header.getBasicVer() < desc.getRequiredVer()) {
          continue;
        }
      }

      Field field = desc.getField();
      Class<?> fieldClass = field.getType();
      field.setAccessible(true);
      Object fieldValue = null;

      try {
        fieldValue = field.get(bean);
      } catch (IllegalArgumentException e) {
        logger.error("BeanCodec:", e);
      } catch (IllegalAccessException e) {
        logger.error("BeanCodec:", e);
      }

      ret = (byte[]) ArrayUtils.addAll(ret, anyCodec.encode(encContextFactory.createEncContext(fieldValue, fieldClass, desc, ctx.getHeader())));
    }

    return ret;
  }

  @Override
  public FieldCodecCategory getCategory() {
    return FieldCodecCategory.BEAN;
  }

  @Override
  public int getStaticByteSize(Class<?> clazz) {

    List<ByteFieldDesc> desces = util.getFieldDesces(clazz);

    if (null == desces || desces.isEmpty()) {
      return -1;
    }

    int staticByteSize = 0;

    for (ByteFieldDesc desc : desces) {
      int fieldByteSize = desc.getByteSize();

      if (fieldByteSize <= 0) {
        fieldByteSize = getStaticByteSize(desc.getFieldType());
      }

      if (fieldByteSize <= 0) {
        return -1;
      }
      staticByteSize += fieldByteSize;
    }

    return staticByteSize;
  }

  @Override
  public DecContextFactory getDecContextFactory() {
    return decContextFactory;
  }

  public void setDecContextFactory(DecContextFactory decContextFactory) {
    this.decContextFactory = decContextFactory;
  }

  @Override
  public EncContextFactory getEncContextFactory() {
    return encContextFactory;
  }

  public void setEncContextFactory(EncContextFactory encContextFactory) {
    this.encContextFactory = encContextFactory;
  }

}
