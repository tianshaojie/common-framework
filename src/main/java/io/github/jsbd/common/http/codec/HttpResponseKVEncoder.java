package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.http.TransportUtil;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.serialization.kv.codec.DefaultKVCodec;
import io.github.jsbd.common.serialization.kv.codec.KVCodec;
import io.github.jsbd.common.serialization.protocol.annotation.Download;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseKVEncoder implements Transformer<Object, HttpResponse> {

  private static final Logger logger      = LoggerFactory.getLogger(HttpResponseKVEncoder.class);

  private KVCodec             kvCodec     = new DefaultKVCodec();
  private boolean             isDebugEnabled;

  private String              contextType = "application/x-tar";

  private boolean             keepAlive   = true;

  @Override
  public HttpResponse transform(Object signal) {
    DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

    resp.setStatus(HttpResponseStatus.OK);
    resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, contextType);

    if (signal instanceof XipSignal) {
      String string = encodeXip((XipSignal) signal);
      if (logger.isDebugEnabled()) {
        logger.debug("signal as string:{} \r\n{} ", string);
      }
      Download down = signal.getClass().getAnnotation(Download.class);
      if (null != string) {
        if (down != null) {
          resp.setStatus(HttpResponseStatus.FOUND);
          String content = string.substring(string.indexOf("=") + 1);
          if (content != null && !"null".equals(content)) {
            resp.headers().set(HttpHeaders.Names.LOCATION, content);
          } else {
            resp.setStatus(HttpResponseStatus.NOT_FOUND);
          }
        }
        resp.setContent(ChannelBuffers.wrappedBuffer(string.getBytes()));
        resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, resp.getContent().writerIndex());
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
      if (this.isKeepAlive() && keepAlive != null) {
        resp.headers().set(HttpHeaders.Names.CONNECTION, keepAlive);
      }
    }

    return resp;
  }

  private String encodeXip(XipSignal signal) {
    String string = getKvCodec().encode(getKvCodec().getEncContextFactory().createEncContext(signal, signal.getClass()));

    return string;
  }

  public KVCodec getKvCodec() {
    return kvCodec;
  }

  public void setKvCodec(KVCodec kvCodec) {
    this.kvCodec = kvCodec;
  }

  public boolean isDebugEnabled() {
    return isDebugEnabled;
  }

  public void setDebugEnabled(boolean isDebugEnabled) {
    this.isDebugEnabled = isDebugEnabled;
  }

  public String getContextType() {
    return contextType;
  }

  public void setContextType(String contextType) {
    this.contextType = contextType;
  }

  public boolean isKeepAlive() {
    return keepAlive;
  }

  public void setKeepAlive(boolean keepAlive) {
    this.keepAlive = keepAlive;
  }
}
