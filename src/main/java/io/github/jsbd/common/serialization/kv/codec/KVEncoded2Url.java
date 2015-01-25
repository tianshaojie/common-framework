package io.github.jsbd.common.serialization.kv.codec;

import io.github.jsbd.common.lang.Transformer;

import java.util.List;
import java.util.Map;

public class KVEncoded2Url implements Transformer<Map<String, List<String>>, String> {

  private String separator = "&";
  private String equal     = "=";

  public String getEqual() {
    return equal;
  }

  public void setEqual(String equal) {
    this.equal = equal;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  @Override
  public String transform(Map<String, List<String>> from) {

    StringBuffer ret = new StringBuffer();

    for (Map.Entry<String, List<String>> e : from.entrySet()) {
      String key = e.getKey();
      List<String> list = e.getValue();

      if (!list.isEmpty()) {
        for (String v : list) {
          ret.append(separator).append(key).append(equal).append(v);
        }
      }

    }
    if (ret.length() > 0) {
      ret.deleteCharAt(0);// remove first separator
    }

    return ret.toString();
  }

}
