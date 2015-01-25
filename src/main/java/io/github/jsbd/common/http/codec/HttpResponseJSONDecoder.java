package io.github.jsbd.common.http.codec;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpResponseJSONDecoder extends OneToOneDecoder {

  private static final Logger  logger    = LoggerFactory.getLogger(HttpResponseJSONDecoder.class);

  private MsgCode2TypeMetainfo typeMetaInfo;

  private int                  dumpBytes = 256;
  private byte[]               encryptKey;
  private Gson                 gson      = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
    logger.debug("HttpResponseJSONDecoder receive msg: {}", msg);

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

      logger.debug(ByteUtil.bytesAsHexString(bytes, dumpBytes));
      XipSignal signal = decodeXipSignal(bytes);

      logger.debug("decoded signal:{}", ToStringBuilder.reflectionToString(signal));
      return signal;
    }
    return msg;
  }

  private XipSignal decodeXipSignal(byte[] bytes) throws Exception {

    byte[] content = bytes;

    try {
      // 对消息体GZIP解压
      content = ZipUtil.uncompress(content);
    } catch (Exception e) {
      logger.error("uncompress failed." + e.getMessage());
      return null;
    }

    // 对消息体进行DES解密
    if (getEncryptKey() != null) {
      try {
        content = DESUtil.decrypt(content, getEncryptKey());
      } catch (Exception e) {
        logger.error("decode decryption failed." + e.getMessage());
        return null;
      }
    }

    // 反序列化成JavaObject
    String messgeStr = new String(content);
    XipMessage message = gson.fromJson(messgeStr.trim(), XipMessage.class);
    XipHeader header = message.getHeader();

    Class<?> type = typeMetaInfo.find(header.getMessageCode());
    if (null == type) {
      logger.error("unknow message code:" + header.getMessageCode());
      return null;
    }

    XipSignal signal = (XipSignal) gson.fromJson(message.getBody(), type);
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

  public byte[] getEncryptKey() {
    return encryptKey;
  }

  public void setEncryptKey(byte[] encryptKey) {
    this.encryptKey = encryptKey;
  }
}
