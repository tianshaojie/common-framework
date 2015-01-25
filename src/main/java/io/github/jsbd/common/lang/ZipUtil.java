package io.github.jsbd.common.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtil {
  // 压缩
  public static byte[] compress(byte[] byteArray) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    gzip.write(byteArray);
    gzip.close();
    byte[] compressByteArray = out.toByteArray();
    return compressByteArray;
  }

  // 解压缩
  public static byte[] uncompress(byte[] byteArry) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream in = new ByteArrayInputStream(byteArry);
    GZIPInputStream gunzip = new GZIPInputStream(in);
    byte[] buffer = new byte[256];
    int n;
    while ((n = gunzip.read(buffer)) >= 0) {
      out.write(buffer, 0, n);
    }
    // toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)
    return out.toByteArray();
  }

  // 测试方法
  public static void main(String[] args) throws IOException {

    // 测试字符串
    String str = "%5B%7B%22lastUpdateTime%22%3A%222011-10-28+9%3A39%3A41%22%2C%22smsList%22%3A%5B%7B%22liveState%22%3A%221";

    // System.out.println("压缩后：" + ZipUtil2.compress(str).length());
    byte[] uncompressByte = ZipUtil.uncompress(ZipUtil.compress(str.getBytes("utf-8")));
    System.out.println("解压缩：" + new String(uncompressByte));
  }
}
