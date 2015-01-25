package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.serialization.bytebean.codec.AnyCodec;
import io.github.jsbd.common.serialization.bytebean.codec.DefaultCodecProvider;
import io.github.jsbd.common.serialization.bytebean.codec.DefaultNumberCodecs;
import io.github.jsbd.common.serialization.bytebean.codec.array.LenArrayCodec;
import io.github.jsbd.common.serialization.bytebean.codec.array.LenListCodec;
import io.github.jsbd.common.serialization.bytebean.codec.bean.BeanFieldCodec;
import io.github.jsbd.common.serialization.bytebean.codec.bean.EarlyStopBeanCodec;
import io.github.jsbd.common.serialization.bytebean.codec.primitive.*;
import io.github.jsbd.common.serialization.bytebean.context.DefaultDecContextFactory;
import io.github.jsbd.common.serialization.bytebean.context.DefaultEncContextFactory;
import io.github.jsbd.common.serialization.bytebean.field.DefaultField2Desc;
import io.github.jsbd.common.serialization.protocol.annotation.SignalCode;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HttpRequestEncoder extends OneToOneEncoder {

  private static final Logger logger    = LoggerFactory.getLogger(HttpRequestEncoder.class);

  private BeanFieldCodec      byteBeanCodec;
  private int                 dumpBytes = 256;
  private boolean             isDebugEnabled;
  private byte[]              encryptKey;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jboss.netty.handler.codec.oneone.OneToOneEncoder#encode(org.jboss.netty
   * .channel.ChannelHandlerContext, org.jboss.netty.channel.Channel,
   * java.lang.Object)
   */
  @Override
  protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

    HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
    if (msg instanceof XipSignal) {
      byte[] bytes = encodeXip((XipSignal) msg);
      request.setHeader("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    } else if (msg instanceof byte[]) {
      byte[] bytes = (byte[]) msg;
      request.setHeader("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    }

    return request;
  }
  private byte[] encodeXip(XipSignal signal) throws Exception {

    SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
    if (null == attr) {
      throw new RuntimeException("invalid signal, no messageCode defined.");
    }

    XipHeader header = createHeader((byte) 1, signal.getIdentification(), attr.messageCode(), 0);

    byte[] bytesBody = getByteBeanCodec().encode(getByteBeanCodec().getEncContextFactory().createEncContext(signal, signal.getClass(), null, header));

    if (getEncryptKey() != null) {
      // 对消息体进行DES加密
      try {
        bytesBody = DESUtil.encrypt(bytesBody, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("encode encryption faield.");
      }
    }

    header.setLength(header.getLength() + bytesBody.length);

    // 更新请求类型
    header.setTypeForClass(signal.getClass());

    byte[] bytes = ArrayUtils.addAll(
        getByteBeanCodec().encode(getByteBeanCodec().getEncContextFactory().createEncContext(header, XipHeader.class, null, null)), bytesBody);

    if (logger.isDebugEnabled() && isDebugEnabled) {
      logger.debug("encode XipSignal", ToStringBuilder.reflectionToString(signal));
      logger.debug("and XipSignal raw bytes -->");
      logger.debug(ByteUtil.bytesAsHexString(bytes, dumpBytes));
    }

    return bytes;
  }

  private XipHeader createHeader(byte basicVer, UUID id, int messageCode, int messageLen) {

    XipHeader header = new XipHeader();
    header.setTransaction(id);
    int headerSize = getByteBeanCodec().getStaticByteSize(XipHeader.class);

    header.setLength(headerSize + messageLen);
    header.setMessageCode(messageCode);
    header.setBasicVer(basicVer);

    return header;
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

  public int getDumpBytes() {
    return dumpBytes;
  }

  public void setDumpBytes(int dumpBytes) {
    this.dumpBytes = dumpBytes;
  }

  public boolean isDebugEnabled() {
    return isDebugEnabled;
  }

  public void setDebugEnabled(boolean isDebugEnabled) {
    this.isDebugEnabled = isDebugEnabled;
  }

  public byte[] getEncryptKey() {
    return encryptKey;
  }

  public void setEncryptKey(byte[] encryptKey) {
    this.encryptKey = encryptKey;
  }

}
