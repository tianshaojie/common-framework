package io.github.jsbd.common.serialization.protocol.xip;

import io.github.jsbd.common.lang.ByteUtil;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.channel.codec.tlv.TLVCodecProviders;
import com.channel.codec.tlv.TLVEncoder;
import com.channel.codec.tlv.TLVEncoderProvider;
import com.channel.utils.ByteUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Xip header
public class XipHeader {

  @Expose
  @SerializedName("bVersion")
  private int  basicVer = 1;

  @Expose
  @SerializedName("bCode")
  private int  messageCode;

  @Expose
  @SerializedName("firstTransaction")
  private long firstTransaction;

  @Expose
  @SerializedName("lastTransaction")
  private long lastTransaction;

  public int getBasicVer() {
    return basicVer;
  }

  public void setBasicVer(int basicVer) {
    this.basicVer = basicVer;
  }

  public long getFirstTransaction() {
    return firstTransaction;
  }

  public void setFirstTransaction(long firstTransaction) {
    this.firstTransaction = firstTransaction;
  }

  public long getLastTransaction() {
    return lastTransaction;
  }

  public void setLastTransaction(long lastTransaction) {
    this.lastTransaction = lastTransaction;
  }

  public int getMessageCode() {
    return messageCode;
  }

  public void setMessageCode(int messageCode) {
    if (messageCode <= 0) {
      throw new RuntimeException("invalid message code.");
    }
    this.messageCode = messageCode;
  }

  public void setTransaction(UUID uuid) {
    this.firstTransaction = uuid.getMostSignificantBits();
    this.lastTransaction = uuid.getLeastSignificantBits();
  }

  public UUID getTransactionAsUUID() {
    return new UUID(this.firstTransaction, this.lastTransaction);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public static void main(String[] args) {
    XipHeader header = new XipHeader();
    header.setBasicVer((byte) 1);
    header.setFirstTransaction(6450626838123529635l);
    header.setLastTransaction(-4941818330698730257l);
    header.setMessageCode(102001);

    TLVEncoderProvider tlvEncoderProvider = TLVCodecProviders.newBigEndianTLVEncoderProvider();
    TLVEncoder<Object> tlvObjectEncoder = tlvEncoderProvider.getObjectEncoder();

    List<byte[]> byteList = tlvObjectEncoder.codec(header, null);
    byte[] content = ByteUtils.union(byteList);
    System.out.println(content.length);
    System.out.println(ByteUtil.bytesAsHexString(content, 256));
  }

}