package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.ZipUtil;
import io.github.jsbd.common.serialization.bytebean.codec.AnyCodec;
import io.github.jsbd.common.serialization.bytebean.codec.DefaultCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.DefaultNumberCodecs;
import io.github.jsbd.common.serialization.bytebean.codec.array.LenArrayCodec;
import io.github.jsbd.common.serialization.bytebean.codec.array.LenListCodec;
import io.github.jsbd.common.serialization.bytebean.codec.bean.BeanFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.bean.EarlyStopBeanCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.ByteCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.CStyleStringCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.IntCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.LenByteArrayCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.LongCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.ShortCodec;
import io.github.jsbd.common.serialization.bytebean.context.DefaultDecContextFactory;
import io.github.jsbd.common.serialization.bytebean.context.DefaultEncContextFactory;
import io.github.jsbd.common.serialization.bytebean.field.DefaultField2Desc;
import io.github.jsbd.common.serialization.protocol.meta.MsgCode2TypeMetainfo;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestDecoder implements Transformer<HttpRequest, Object> {

  private static final Logger  logger    = LoggerFactory.getLogger(HttpRequestDecoder.class);

  private BeanFieldCodec       byteBeanCodec;
  private MsgCode2TypeMetainfo typeMetaInfo;

  private int                  dumpBytes = 256;
  private boolean              isDebugEnabled;
  private byte[]               encryptKey;

  @Override
  public Object transform(HttpRequest request) {

    if (null != request.getContent()) {
      ChannelBuffer content = request.getContent();
      byte[] bytes = content.array();
      if (logger.isDebugEnabled() && isDebugEnabled) {
        logger.debug(ByteUtil.bytesAsHexString(bytes, dumpBytes));
      }
      boolean isPress = false;
      if (request.getHeader("isPress") != null) {
        isPress = true;
      }

      XipSignal signal = decodeXipSignal(bytes, isPress);
      if (logger.isDebugEnabled() && isDebugEnabled) {
        logger.debug("decoded signal:{}", ToStringBuilder.reflectionToString(signal));
      }
      return signal;
    }

    return null;
  }

  private XipSignal decodeXipSignal(byte[] bytes, boolean isPress) {

    XipHeader header = (XipHeader) getByteBeanCodec().decode(
        getByteBeanCodec().getDecContextFactory().createDecContext(bytes, XipHeader.class, null, null, null)).getValue();

    Class<?> type = typeMetaInfo.find(header.getMessageCode());
    if (null == type) {
      throw new RuntimeException("unknow message code:" + header.getMessageCode());
    }

    byte[] bodyBytes = ArrayUtils.subarray(bytes, XipHeader.HEADER_LENGTH, bytes.length);

    // 对消息体进行DES解密
    if (getEncryptKey() != null) {
      try {
        bodyBytes = DESUtil.decrypt(bodyBytes, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("decode decryption failed." + e.getMessage());

      }
    }
    if (isPress) {
      try {
        bodyBytes = ZipUtil.uncompress(bodyBytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    XipSignal signal = (XipSignal) getByteBeanCodec().decode(getByteBeanCodec().getDecContextFactory().createDecContext(bodyBytes, type, null, null, header))
        .getValue();

    if (null != signal) {
      signal.setIdentification(header.getTransactionAsUUID());
    }

    return signal;
  }

  public void setByteBeanCodec(BeanFieldCodec byteBeanCodec) {
    this.byteBeanCodec = byteBeanCodec;
  }

  public BeanFieldCodec getByteBeanCodec() {
    if (byteBeanCodec == null) {
      DefaultCodecProvider codecProvider = new DefaultCodecProvider();

      // 初始化解码器集合
      codecProvider.addCodec(new AnyCodec()).addCodec(new ByteCodec()).addCodec(new ShortCodec()).addCodec(new IntCodec()).addCodec(new LongCodec())
          .addCodec(new CStyleStringCodec()).addCodec(new LenByteArrayCodec()).addCodec(new LenListCodec()).addCodec(new LenArrayCodec());

      // 对象解码器需要指定字段注释读取方法
      EarlyStopBeanCodec byteBeanCodec = new EarlyStopBeanCodec(new DefaultField2Desc());
      codecProvider.addCodec(byteBeanCodec);

      DefaultEncContextFactory encContextFactory = new DefaultEncContextFactory();
      DefaultDecContextFactory decContextFactory = new DefaultDecContextFactory();

      encContextFactory.setCodecProvider(codecProvider);
      encContextFactory.setNumberCodec(DefaultNumberCodecs.getLittleEndianNumberCodec());

      decContextFactory.setCodecProvider(codecProvider);
      decContextFactory.setNumberCodec(DefaultNumberCodecs.getLittleEndianNumberCodec());

      byteBeanCodec.setDecContextFactory(decContextFactory);
      byteBeanCodec.setEncContextFactory(encContextFactory);

      this.byteBeanCodec = byteBeanCodec;
    }
    return byteBeanCodec;
  }

  public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
    this.typeMetaInfo = typeMetaInfo;
  }

  public void setDumpBytes(int dumpBytes) {
    this.dumpBytes = dumpBytes;
  }

  public MsgCode2TypeMetainfo getTypeMetaInfo() {
    return typeMetaInfo;
  }

  public int getDumpBytes() {
    return dumpBytes;
  }

  public boolean isDebugEnabled() {
    return isDebugEnabled;
  }

  public void setDebugEnabled(boolean isDebugEnabled) {
    this.isDebugEnabled = isDebugEnabled;
  }

  /**
   * @return the encryptKey
   */
  public byte[] getEncryptKey() {
    return encryptKey;
  }

  /**
   * @param encryptKey
   *          the encryptKey to set
   */
  public void setEncryptKey(byte[] encryptKey) {
    this.encryptKey = encryptKey;
  }

}
