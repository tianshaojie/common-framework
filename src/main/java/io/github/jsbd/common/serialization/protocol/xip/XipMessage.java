package io.github.jsbd.common.serialization.protocol.xip;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class XipMessage {

  @Expose
  @SerializedName("head")
  private XipHeader header;

  @Expose
  @SerializedName("body")
  private String    body;

  public XipHeader getHeader() {
    return header;
  }

  public void setHeader(XipHeader header) {
    this.header = header;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
