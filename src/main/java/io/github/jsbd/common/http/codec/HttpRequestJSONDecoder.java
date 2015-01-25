package io.github.jsbd.common.http.codec;

import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.ZipUtil;
import io.github.jsbd.common.serialization.protocol.meta.MsgCode2TypeMetainfo;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipMessage;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;

import java.io.IOException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpRequestJSONDecoder implements Transformer<HttpRequest, Object> {

  private static final Logger  logger    = LoggerFactory.getLogger(HttpRequestJSONDecoder.class);

  private MsgCode2TypeMetainfo typeMetaInfo;

  private int                  dumpBytes = 256;
  private boolean              isDebugEnabled;
  private byte[]               encryptKey;
  private Gson                 gson      = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  @Override
  public Object transform(HttpRequest request) {

    if (null != request.getContent()) {
      ChannelBuffer content = request.getContent();
      byte[] bytes = new byte[content.readableBytes()];
      content.readBytes(bytes);

      if (logger.isDebugEnabled() && isDebugEnabled) {
        logger.debug(ByteUtil.bytesAsHexString(bytes, dumpBytes));
      }
      boolean isPress = false;
      if (request.getHeader("isPress") != null) {
        isPress = true;
      }
      XipSignal signal = decodeXipSignal(bytes, isPress);
      if (logger.isDebugEnabled() && isDebugEnabled) {
        logger.debug("decoded signal:{}", ToStringBuilder.reflectionToString(signal));
      }
      return signal;
    }

    return null;
  }

  private XipSignal decodeXipSignal(byte[] bytes, boolean isPress) {

    byte[] content = bytes;
    // 对消息体进行DES解密
    if (getEncryptKey() != null) {
      try {
        content = DESUtil.decrypt(content, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("decode decryption failed." + e.getMessage());

      }
    }
    if (isPress) {
      try {
        content = ZipUtil.uncompress(content);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    String xipMessgeStr = new String(content);
    XipMessage xipMessage = gson.fromJson(xipMessgeStr.trim(), XipMessage.class);
    if (xipMessage == null) {
      logger.warn("invalid xipmessage");
      return null;
    }
    XipHeader header = gson.fromJson(xipMessage.getXipHeader(), XipHeader.class);

    Class<?> type = typeMetaInfo.find(header.getMessageCode());
    if (null == type) {
      throw new RuntimeException("unknow message code:" + header.getMessageCode());
    }

    XipSignal signal = (XipSignal) gson.fromJson(xipMessage.getXipBody(), type);

    if (null != signal) {
      signal.setIdentification(header.getTransactionAsUUID());
    }

    return signal;
  }

  public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
    this.typeMetaInfo = typeMetaInfo;
  }

  public void setDumpBytes(int dumpBytes) {
    this.dumpBytes = dumpBytes;
  }

  public MsgCode2TypeMetainfo getTypeMetaInfo() {
    return typeMetaInfo;
  }

  public int getDumpBytes() {
    return dumpBytes;
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
