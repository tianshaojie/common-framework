package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.http.TransportUtil;
import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.ZipUtil;
import io.github.jsbd.common.serialization.protocol.annotation.SignalCode;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipMessage;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpResponseJSONEncoder implements Transformer<Object, HttpResponse> {

  private static final Logger logger    = LoggerFactory.getLogger(HttpResponseJSONEncoder.class);

  private int                 dumpBytes = 256;
  private boolean             isDebugEnabled;
  private byte[]              encryptKey;
  private Gson                gson      = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  @Override
  public HttpResponse transform(Object signal) {
    DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    resp.setStatus(HttpResponseStatus.OK);
    resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/x-tar");

    if (signal instanceof XipSignal) {
      byte[] bytes = encodeXip((XipSignal) signal, resp);

      logger.debug("HttpResponseJSONEncoder encode as hex:{} \r\n{} ", ByteUtil.bytesAsHexString(bytes, dumpBytes));

      if (null != bytes) {
        resp.setContent(ChannelBuffers.wrappedBuffer(bytes));
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
      }
    }

    // resp.setHeader("Content-Range", contentRange);
    // resp.setHeader("Date", date);
    HttpRequest req = TransportUtil.getRequestOf(signal);
    if (req != null) {
      String uuid = req.headers().get("uuid");
      if (StringUtils.isNotEmpty(uuid)) {
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

  private byte[] encodeXip(XipSignal signal, DefaultHttpResponse resp) {

    SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
    if (null == attr) {
      throw new RuntimeException("invalid signal, no messageCode defined.");
    }

    // GSON序列化
    XipMessage xipMessage = new XipMessage();
    XipHeader header = createHeader((byte) 1, signal.getIdentification(), attr.messageCode());
    xipMessage.setHeader(header);
    xipMessage.setBody(gson.toJson(signal));
    byte[] content = null;
    try {
      content = gson.toJson(xipMessage).getBytes("utf-8");
    } catch (UnsupportedEncodingException e1) {
      throw new RuntimeException("toJson failed." + e1.getMessage());
    }

    // 对消息体进行DES加密
    if (getEncryptKey() != null) {
      try {
        content = DESUtil.encrypt(content, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("encode decryption failed." + e.getMessage());
      }
    }

    // GZIP压缩
    try {
      content = ZipUtil.compress(content);
    } catch (Exception e) {
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
