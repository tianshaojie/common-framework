package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.ZipUtil;
import io.github.jsbd.common.serialization.protocol.annotation.SignalCode;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipMessage;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;

import java.io.IOException;
import java.util.UUID;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpRequestJSONEncoder extends OneToOneEncoder {

  private static final Logger logger    = LoggerFactory.getLogger(HttpRequestJSONEncoder.class);

  private int                 dumpBytes = 256;
  private byte[]              encryptKey;
  private Gson                gson      = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  @Override
  protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

    HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");

    if (msg instanceof XipSignal) {
      byte[] bytes = encodeXip((XipSignal) msg, request);
      request.headers().set("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    } else if (msg instanceof byte[]) {
      byte[] bytes = (byte[]) msg;
      request.headers().set("Content-Length", bytes.length);
      request.setContent(ChannelBuffers.wrappedBuffer(bytes));
    }

    return request;
  }

  private byte[] encodeXip(XipSignal signal, HttpRequest request) throws Exception {

    SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
    if (null == attr) {
      throw new RuntimeException("invalid signal, no messageCode defined.");
    }

    // 将消息序列化成json
    XipMessage xipMessage = new XipMessage();
    XipHeader header = createHeader((byte) 1, signal.getIdentification(), attr.messageCode());
    xipMessage.setHeader(header);
    xipMessage.setBody(gson.toJson(signal));
    byte[] content = gson.toJson(xipMessage).getBytes("utf-8");

    // 对消息体进行DES加密
    if (getEncryptKey() != null) {
      try {
        content = DESUtil.encrypt(content, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("encode encryption failed." + e.getMessage());

      }
    }

    // 对消息体进行压缩
    try {
      content = ZipUtil.compress(content);
    } catch (IOException e) {
      logger.error("err in compress content,e=[{}]", e.getCause());
    }

    return content;
  }

  private XipHeader createHeader(byte basicVer, UUID uuid, int messageCode) {
    XipHeader header = new XipHeader();
    header.setTransaction(uuid);
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

  public byte[] getEncryptKey() {
    return encryptKey;
  }

  public void setEncryptKey(byte[] encryptKey) {
    this.encryptKey = encryptKey;
  }

}
