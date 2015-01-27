package io.github.jsbd.common.http.codec;

import com.channel.codec.tlv.TLVEncoder;
import com.channel.codec.tlv.TLVEncoderProvider;
import com.channel.utils.ByteUtils;
import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
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

import java.util.List;
import java.util.UUID;

public class HttpRequestTLVEncoder extends OneToOneEncoder {

  private static final Logger logger    = LoggerFactory.getLogger(HttpRequestTLVEncoder.class);

  private int                 dumpBytes = 256;
  private boolean             isDebugEnabled;
  private TLVEncoderProvider  tlvEncoderProvider;
  private byte[]              encryptKey;

  @Override
  protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

    HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");

    if (msg instanceof XipSignal) {
      byte[] bytes = encodeXip((XipSignal) msg);
      request.headers().set("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    } else if (msg instanceof byte[]) {
      byte[] bytes = (byte[]) msg;
      request.headers().set("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    }

    return request;
  }
  private byte[] encodeXip(XipSignal signal) throws Exception {

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
        throw new RuntimeException("encode encryption failed." + e.getMessage());

      }
    }

    XipHeader header = createHeader((byte) 1, signal.getIdentification(), attr.messageCode(), bodyBytes.length);

    // 更新请求类型
    header.setTypeForClass(signal.getClass());

    List<byte[]> headByteList = tlvObjectEncoder.codec(header, null);
    byte[] headBytes = ByteUtils.union(headByteList);

    byte[] bytes = ArrayUtils.addAll(headBytes, bodyBytes);

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

    int headerSize = XipHeader.TLV_HEADER_LENGTH;

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
