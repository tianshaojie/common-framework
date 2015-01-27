package io.github.jsbd.common.http.codec;

import com.channel.codec.tlv.TLVEncoder;
import com.channel.codec.tlv.TLVEncoderProvider;
import com.channel.utils.ByteUtils;
import io.github.jsbd.common.http.TransportUtil;
import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.serialization.protocol.annotation.SignalCode;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class HttpResponseTLVEncoder implements Transformer<Object, HttpResponse> {

  private static final Logger logger    = LoggerFactory.getLogger(HttpResponseTLVEncoder.class);

  private int                 dumpBytes = 256;
  private boolean             isDebugEnabled;
  private TLVEncoderProvider  tlvEncoderProvider;
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

    TLVEncoder<Object> tlvObjectEncoder = tlvEncoderProvider.getObjectEncoder();

    List<byte[]> byteList = tlvObjectEncoder.codec(signal, null);
    byte[] bodyBytes = ByteUtils.union(byteList);

    SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
    if (null == attr) {
      throw new RuntimeException("invalid signal, no messageCode defined.");
    }

    // 对消息体进行DES解密
    if (getEncryptKey() != null) {
      try {
        bodyBytes = DESUtil.encrypt(bodyBytes, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("encode decryption failed." + e.getMessage());

      }
    }

    XipHeader header = createHeader((byte) 1, signal.getIdentification(), attr.messageCode(), bodyBytes.length);

    // 更新请求类型
    header.setTypeForClass(signal.getClass());

    List<byte[]> headByteList = tlvObjectEncoder.codec(header, null);
    byte[] headBytes = ByteUtils.union(headByteList);

    byte[] bytes = ArrayUtils.addAll(headBytes, bodyBytes);

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

    int headerSize = XipHeader.TLV_HEADER_LENGTH;// 暂时固定

    header.setLength(headerSize + messageLen);
    header.setMessageCode(messageCode);
    header.setBasicVer(basicVer);

    return header;
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

  public void setTlvEncoderProvider(TLVEncoderProvider tlvEncoderProvider) {
    this.tlvEncoderProvider = tlvEncoderProvider;
  }

  public byte[] getEncryptKey() {
    return encryptKey;
  }

  public void setEncryptKey(byte[] encryptKey) {
    this.encryptKey = encryptKey;
  }

}
