package io.github.jsbd.common.http.codec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jsbd.common.lang.ByteUtil;
import io.github.jsbd.common.lang.DESUtil;
import io.github.jsbd.common.lang.ZipUtil;
import io.github.jsbd.common.serialization.protocol.meta.MsgCode2TypeMetainfo;
import io.github.jsbd.common.serialization.protocol.xip.XipHeader;
import io.github.jsbd.common.serialization.protocol.xip.XipMessage;
import io.github.jsbd.common.serialization.protocol.xip.XipSignal;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseJSONDecoder extends OneToOneDecoder {

  private static final Logger  logger    = LoggerFactory.getLogger(HttpResponseJSONDecoder.class);

  private MsgCode2TypeMetainfo typeMetaInfo;

  private int                  dumpBytes = 256;
  private boolean              isDebugEnabled;
  private byte[]               encryptKey;
  private Gson                 gson      = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.jboss.netty.handler.codec.oneone.OneToOneDecoder#decode(org.jboss.netty
   * .channel.ChannelHandlerContext, org.jboss.netty.channel.Channel,
   * java.lang.Object)
   */
  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug("decode {}", msg);
    }

    if (msg instanceof HttpResponse) {
      HttpResponse response = (HttpResponse) msg;
      if (response.getStatus().getCode() != 200) {
        return msg;
      }

      ChannelBuffer content = response.getContent();
      if (!content.readable()) {
        return msg;
      }
      byte[] bytes = new byte[content.readableBytes()];
      content.readBytes(bytes);
      boolean isCompress = false;
      if (response.getHeader("isPress") != null) {
        isCompress = true;
      }
      if (logger.isDebugEnabled() && isDebugEnabled) {
        logger.debug(ByteUtil.bytesAsHexString(bytes, dumpBytes));
      }
      XipSignal signal = decodeXipSignal(bytes, isCompress);
      if (logger.isDebugEnabled() && isDebugEnabled) {
        logger.debug("decoded signal:{}", ToStringBuilder.reflectionToString(signal));
      }
      return signal;
    }
    return msg;
  }

  private XipSignal decodeXipSignal(byte[] bytes, boolean isCompress) throws Exception {

    byte[] content = bytes;
    // 对消息体进行DES解密
    if (getEncryptKey() != null) {
      try {
        content = DESUtil.decrypt(content, getEncryptKey());
      } catch (Exception e) {
        throw new RuntimeException("decode decryption failed." + e.getMessage());

      }
    }

    if (isCompress) {
      content = ZipUtil.uncompress(content);
    }
    String xipMessgeStr = new String(content);
    XipMessage xipMessage = gson.fromJson(xipMessgeStr.trim(), XipMessage.class);
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
