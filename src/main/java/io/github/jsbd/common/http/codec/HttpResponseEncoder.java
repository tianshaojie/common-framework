package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.http.TransportUtil;
import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.Transformer;
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
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class HttpResponseEncoder implements Transformer<Object, HttpResponse> {

  private static final Logger logger    = LoggerFactory.getLogger(HttpResponseEncoder.class);

  private BeanFieldCodec      byteBeanCodec;
  private int                 dumpBytes = 256;
  private boolean             isDebugEnabled;
  private byte[]              encryptKey;

  @Override
  public HttpResponse transform(Object signal) {
    DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

    resp.setStatus(HttpResponseStatus.OK);
    resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/x-tar");

    if (signal instanceof XipSignal) {
      byte[] bytes = encodeXip((XipSignal) signal);
      if (logger.isDebugEnabled()) {
        logger.debug("signal as hex:{} \r\n{} ", ByteUtil.bytesAsHexString(bytes, dumpBytes));
      }
      if (null != bytes) {
        resp.setContent(ChannelBuffers.wrappedBuffer(bytes));
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
      }
    }

    HttpRequest req = TransportUtil.getRequestOf(signal);
    if (req != null) {
      String uuid = req.headers().get("uuid");
      if (uuid != null) {
        resp.headers().set("uuid", uuid);
      }

      // 是否需要持久连接
      String keepAlive = req.headers().get(HttpHeaders.Names.CONNECTION);
      if (keepAlive != null) {
        resp.headers().set(HttpHeaders.Names.CONNECTION, keepAlive);
      }
    }

    return resp;
  }

  private byte[] encodeXip(XipSignal signal) {

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

    // 更新请求类型
    header.setTypeForClass(signal.getClass());
    header.setLength(header.getLength() + bytesBody.length);
    byte[] bytes = ArrayUtils.addAll(
        getByteBeanCodec().encode(getByteBeanCodec().getEncContextFactory().createEncContext(header, XipHeader.class, null, header)), bytesBody);

    if (logger.isDebugEnabled() && isDebugEnabled) {
      logger.debug("encode XipSignal:" + signal);
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
          .addCodec(new CStyleStringCodec()).addCodec(new LenByteArrayCodec()).addCodec(new LenListCodec()).addCodec(new LenArrayCodec())
          .addCodec(new FloatCodec());

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
