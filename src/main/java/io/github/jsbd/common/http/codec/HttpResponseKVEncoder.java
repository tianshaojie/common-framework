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
    resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, contextType);

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
            resp.setHeader(HttpHeaders.Names.LOCATION, content);
          } else {
            resp.setStatus(HttpResponseStatus.NOT_FOUND);
          }
        }
        resp.setContent(ChannelBuffers.wrappedBuffer(string.getBytes()));
        resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, resp.getContent().writerIndex());
      }
    }

    HttpRequest req = TransportUtil.getRequestOf(signal);
    if (req != null) {
      String uuid = req.getHeader("uuid");
      if (uuid != null) {
        resp.setHeader("uuid", uuid);
      }

      // 是否需要持久连接
      String keepAlive = req.getHeader(HttpHeaders.Names.CONNECTION);
      if (this.isKeepAlive() && keepAlive != null) {
        resp.setHeader(HttpHeaders.Names.CONNECTION, keepAlive);
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

  /**
   * @return the contextType
   */
  public String getContextType() {
    return contextType;
  }

  /**
   * @param contextType
   *          the contextType to set
   */
  public void setContextType(String contextType) {
    this.contextType = contextType;
  }

  /**
   * @return the keepAlive
   */
  public boolean isKeepAlive() {
    return keepAlive;
  }

  /**
   * @param keepAlive
   *          the keepAlive to set
   */
  public void setKeepAlive(boolean keepAlive) {
    this.keepAlive = keepAlive;
  }
}
