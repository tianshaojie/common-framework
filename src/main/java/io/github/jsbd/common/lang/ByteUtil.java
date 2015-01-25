package io.github.jsbd.common.lang;

import java.util.List;

public class ByteUtil {

  public static String bytesAsHexString(byte[] bytes, int maxShowBytes) {
    int idx = 0;
    StringBuilder body = new StringBuilder();
    body.append("bytes size is:[");
    body.append(bytes.length);
    body.append("]\r\n");

    for (byte b : bytes) {
      int hex = ((int) b) & 0xff;
      String shex = Integer.toHexString(hex).toUpperCase();
      if (1 == shex.length()) {
        body.append("0");
      }
      body.append(shex);
      body.append(" ");
      idx++;
      if (16 == idx) {
        body.append("\r\n");
        idx = 0;
      }
      maxShowBytes--;
      if (maxShowBytes <= 0) {
        break;
      }
    }
    if (idx != 0) {
      body.append("\r\n");
    }
    return body.toString();
  }

  public static byte[] union(List<byte[]> byteList) {
    int size = 0;
    for (byte[] bs : byteList) {
      size += bs.length;
    }
    byte[] ret = new byte[size];
    int pos = 0;
    for (byte[] bs : byteList) {
      System.arraycopy(bs, 0, ret, pos, bs.length);
      pos += bs.length;
    }
    return ret;
  }

  public static int totalByteSizeOf(List<byte[]> byteList) {
    int len = 0;
    for (byte[] bs : byteList) {
      len += bs.length;
    }
    return len;
  }

  /**
   * Turns an array of bytes into a String representing each byte as an unsigned
   * hex number.
   * <p/>
   * Method by Santeri Paavolainen, Helsinki Finland 1996<br>
   * (c) Santeri Paavolainen, Helsinki Finland 1996<br>
   * Distributed under LGPL.
   * 
   * @param hash
   *          an array of bytes to convert to a hex-string
   * @return generated hex string
   */
  public static String byte2Hex(byte hash[]) {
    StringBuffer buf = new StringBuffer(hash.length * 2);
    int i;

    for (i = 0; i < hash.length; i++) {
      if (((int) hash[i] & 0xff) < 0x10) {
        buf.append("0");
      }
      buf.append(Long.toString((int) hash[i] & 0xff, 16));
    }
    return buf.toString();
  }

  public static byte[] hex2byte(byte[] b) {

    if ((b.length % 2) != 0)
      throw new IllegalArgumentException("The length is not even.");

    byte[] b2 = new byte[b.length / 2];
    for (int n = 0; n < b.length; n += 2) {
      String item = new String(b, n, 2);
      b2[n / 2] = (byte) Integer.parseInt(item, 16);
    }
    return b2;
  }
}
