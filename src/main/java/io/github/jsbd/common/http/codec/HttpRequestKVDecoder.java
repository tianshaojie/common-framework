package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.serialization.kv.codec.DefaultKVCodec;
import io.github.jsbd.common.serialization.kv.codec.KVCodec;
import io.github.jsbd.common.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;

public class HttpRequestKVDecoder implements Transformer<HttpRequest, Object> {

  private KVCodec              kvCodec = new DefaultKVCodec();

  private MsgCode2TypeMetainfo typeMetaInfo;

  public Object transform(HttpRequest request) {
    String uri = request.getUri().trim();
    int messageCode = Integer.parseInt(getRequestCode(uri));
    Class<?> type = typeMetaInfo.find(messageCode);
    if (null == type) {
      throw new RuntimeException("unknow message code:" + messageCode);
    }

    String queryString = "";
    int idx = uri.indexOf('?');
    if (-1 != idx) {
      queryString = uri.substring(idx + 1); // escape '?' character and
                                            // more
    }

    if (request.getContent().readable()) {
      queryString = "".equals(queryString) ? request.getContent().toString(CharsetUtil.UTF_8) : queryString + "&"
          + request.getContent().toString(CharsetUtil.UTF_8);
    }
    return kvCodec.decode(kvCodec.getDecContextFactory().createDecContext(queryString, type, null));
  }

  private String getRequestCode(String uri) {
    String requestCode = uri.trim();

    if (requestCode.startsWith("/")) {
      requestCode = requestCode.substring(1);
    }
    if (requestCode.endsWith("/")) {
      requestCode = requestCode.substring(0, requestCode.length() - 1);
    }

    // for eg: http://appid.fivesky.net:4009/UpdateProvision
    int idx = requestCode.lastIndexOf('/');
    if (-1 != idx) {
      requestCode = requestCode.substring(idx + 1); // escape '/' character
    }

    // for eg: UpdateProvision?param1=111&param2=222
    idx = requestCode.indexOf('?');
    if (-1 != idx) {
      requestCode = requestCode.substring(0, idx); // escape '?' character and
                                                   // more
    }
    return requestCode.trim();
  }

  public MsgCode2TypeMetainfo getTypeMetaInfo() {
    return typeMetaInfo;
  }

  public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
    this.typeMetaInfo = typeMetaInfo;
  }

  public KVCodec getKvCodec() {
    return kvCodec;
  }

  public void setKvCodec(KVCodec kvCodec) {
    this.kvCodec = kvCodec;
  }

}
