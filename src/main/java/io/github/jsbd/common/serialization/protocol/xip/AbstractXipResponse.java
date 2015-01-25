package io.github.jsbd.common.serialization.protocol.xip;

import com.channel.codec.tlv.annotation.TLVAttribute;
import com.google.gson.annotations.Expose;
import io.github.jsbd.common.serialization.bytebean.annotation.ByteField;

public class AbstractXipResponse extends AbstractXipSignal implements XipResponse {

  @Expose
  @TLVAttribute(tag = 1)
  @ByteField(index = 0, description = "错误代码")
  private int    errorCode;

  @Expose
  @TLVAttribute(tag = 2)
  @ByteField(index = 1, description = "提示消息内容")
  private String errorMessage;

  public static <T extends AbstractXipResponse> T createRespForError(Class<T> clazz, int errorCode, String errorMessage) {
    T resp;
    try {
      resp = clazz.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    resp.setErrorCode(errorCode);
    resp.setErrorMessage(errorMessage);
    return resp;
  }

  public int getErrorCode() {
    return this.errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}