package io.github.jsbd.common.serialization.bytebean.codec.array;

import io.github.jsbd.common.serialization.bytebean.codec.ByteFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.FieldCodecCategory;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.AbstractPrimitiveCodec;
import io.github.jsbd.common.serialization.bytebean.context.DecContext;
import io.github.jsbd.common.serialization.bytebean.context.DecResult;
import io.github.jsbd.common.serialization.bytebean.context.EncContext;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class LenListCodec extends AbstractPrimitiveCodec implements ByteFieldCodec {

  private static final Logger logger = LoggerFactory.getLogger(LenListCodec.class);

  @Override
  public FieldCodecCategory getCategory() {
    return null;
  }

  @Override
  public DecResult decode(DecContext ctx) {

    // 默认4个字节存储List长度
    DecResult ret = ctx.getCodecOf(int.class).decode(
        ctx.getDecContextFactory().createDecContext(ctx.getDecBytes(), int.class, ctx.getDecOwner(), null, ctx.getHeader()));
    int listLength = (Integer) ret.getValue();
    byte[] bytes = ret.getRemainBytes();
    Class<?> compomentClass = getCompomentClass(ctx.getField());

    ArrayList<Object> list = new ArrayList<Object>();
    if (listLength > 0) {
      ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

      for (int idx = 0; idx < listLength; idx++) {
        ret = anyCodec.decode(ctx.getDecContextFactory().createDecContext(bytes, compomentClass, ctx.getDecOwner(), null, ctx.getHeader()));
        list.add(ret.getValue());
        bytes = ret.getRemainBytes();
      }
    }

    return new DecResult(list, bytes);
  }

  @SuppressWarnings("unchecked")
  @Override
  public byte[] encode(EncContext ctx) {
    ArrayList<Object> list = (ArrayList<Object>) ctx.getEncObject();
    int listLength = (null != list ? list.size() : 0);
    Class<?> compomentClass = getCompomentClass(ctx.getField());

    // 默认4个字节存储List长度
    byte[] bytes = ctx.getCodecOf(int.class).encode(ctx.getEncContextFactory().createEncContext(listLength, int.class, null, ctx.getHeader()));

    if (listLength > 0) {

      ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

      for (int idx = 0; idx < listLength; idx++) {
        bytes = ArrayUtils.addAll(bytes, anyCodec.encode(ctx.getEncContextFactory().createEncContext(list.get(idx), compomentClass, null, ctx.getHeader())));
      }
    }
    return bytes;
  }

  @Override
  public Class<?>[] getFieldType() {
    return new Class<?>[] { ArrayList.class };
  }

  public Class<?> getCompomentClass(Field field) {
    if (null == field) {
      String errmsg = "LenListCodec: field is null, can't get compoment class.";
      logger.error(errmsg);
      throw new RuntimeException(errmsg);
    }
    Type type = field.getGenericType();

    if (null == type || !(type instanceof ParameterizedType)) {
      String errmsg = "LenListCodec: getGenericType invalid, can't get compoment class." + "/ cause field is [" + field + "]";
      logger.error(errmsg);
      throw new RuntimeException(errmsg);
    }
    ParameterizedType parameterizedType = (ParameterizedType) type;
    Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
    return clazz;
  }

}
