package io.github.jsbd.common.serialization.protocol.xip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class XipMessage {

  @Expose
  @SerializedName("head")
  private String xipHeader;

  @Expose
  @SerializedName("body")
  private String xipBody;

  public String getXipHeader() {
    return xipHeader;
  }

  public void setXipHeader(String xipHeader) {
    this.xipHeader = xipHeader;
  }

  public String getXipBody() {
    return xipBody;
  }

  public void setXipBody(String xipBody) {
    this.xipBody = xipBody;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public static void main(String[] args) {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    XipMessage msg = new XipMessage();
    XipHeader head = new XipHeader();
    head.setMessageCode(10001);
    msg.setXipHeader(head.toString());
    String str = gson.toJson(msg);
    System.out.println(str);
    XipMessage msg1 = gson.fromJson(str, XipMessage.class);
    System.out.println(msg1);

    // String s1 =
    // "{\"basicVer\":1,\"type\":1,\"firstTransaction\":0,\"secondTransaction\":0,\"messageCode\":10001}";
    // XipMessage msg2 = gson.fromJson(s1, XipMessage.class);
  }
}
