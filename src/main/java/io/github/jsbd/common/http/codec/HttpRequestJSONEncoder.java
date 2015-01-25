package io.github.jsbd.common.http.codec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.ZipUtil;
import io.github.jsbd.common.serialization.protocol.annotation.Compress;
import io.github.jsbd.common.serialization.protocol.annotation.SignalCode;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipMessage;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;
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

import java.io.IOException;
import java.util.UUID;

public class HttpRequestJSONEncoder extends OneToOneEncoder {

  private static final Logger logger    = LoggerFactory.getLogger(HttpRequestJSONEncoder.class);

  private int                 dumpBytes = 256;
  private boolean             isDebugEnabled;
  private byte[]              encryptKey;
  private Gson                gson      = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

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
      byte[] bytes = encodeXip((XipSignal) msg, request);
      request.setHeader("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    } else if (msg instanceof byte[]) {
      byte[] bytes = (byte[]) msg;
      request.setHeader("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    }

    return request;
  }
  private byte[] encodeXip(XipSignal signal, HttpRequest request) throws Exception {

    SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
    if (null == attr) {
      throw new RuntimeException("invalid signal, no messageCode defined.");
    }

    XipHeader header = createHeader((byte) 1, signal.getIdentification(), attr.messageCode(), 1);

    // 更新请求类型
    header.setTypeForClass(signal.getClass());

    XipMessage xipMessage = new XipMessage();
    xipMessage.setXipHeader(gson.toJson(header));
    xipMessage.setXipBody(gson.toJson(signal));

    byte[] content = gson.toJson(xipMessage).getBytes("utf-8");

    Compress press = signal.getClass().getAnnotation(Compress.class);
    if (press != null) {
      try {
        content = ZipUtil.compress(content);
        request.setHeader("isPress", true);
      } catch (IOException e) {
        logger.error("err in compress content,e=[{}]", e.getCause());
      }
    }

    // 对消息体进行DES加密
    if (getEncryptKey() != null) {
      try {
        content = DESUtil.encrypt(content, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("encode encryption failed." + e.getMessage());

      }
    }
    return content;
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
  public byte[] getEncryptKey() {
    return encryptKey;
  }
  public void setEncryptKey(byte[] encryptKey) {
    this.encryptKey = encryptKey;
  }

}
