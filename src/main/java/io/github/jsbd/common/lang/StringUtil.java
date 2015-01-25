package io.github.jsbd.common.lang;

import java.util.Arrays;

public class StringUtil {

    /**
     * 将下划线连接的字符串替换为驼峰风格,方便JavaBean拷贝
     * <p/>
     * <h2>Example:</h2>
     * <code>toCamelCasing("pic_path")</code> will return picPath
     *
     * @param s
     * @return
     */
    public static String toCamelCasing(String s) {
        if (s == null) {
            return s;
        }

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < s.length() - 1; i++) {
            char ch = s.charAt(i);
            if (ch != '_') {
                buffer.append(ch);
            } else {
                char nextChar = s.charAt(i + 1);
                if (nextChar != '_') {
                    if (buffer.toString().length() < 2) {
                        buffer.append(Character.toLowerCase(nextChar));
                    } else {
                        buffer.append(Character.toUpperCase(nextChar));
                    }
                    i++;
                }
            }
        }
        char lastChar = s.charAt(s.length() - 1);
        if (lastChar != '_') {
            buffer.append(lastChar);
        }

        return buffer.toString();
    }

    public static String rightPad(String text, int length, char c) {
        if (text == null) {
            return text;
        }
        if (text.length() >= length) {
            return text;
        }

        char[] array = new char[length];
        System.arraycopy(text.toCharArray(), 0, array, 0, text.length());
        Arrays.fill(array, text.length(), length, c);
        return new String(array);
    }

    /**
     * Turns an array of bytes into a String representing each byte as an
     * unsigned hex number.
     * <p/>
     * Method by Santeri Paavolainen, Helsinki Finland 1996<br>
     * (c) Santeri Paavolainen, Helsinki Finland 1996<br>
     * Distributed under LGPL.
     *
     * @param hash an array of bytes to convert to a hex-string
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

    /**
     * 替换字符串中指定位置的字符为相应的字符串
     *
     * @param text    源字符串
     * @param index   第几个下标
     * @param destStr 目标字符串
     * @return
     */
    public static String replaceIndex(String text, int index, String destStr) {
        if (index < 0) {
            throw new IllegalArgumentException("The index is less than 0.");
        }

        return text.substring(0, index) + destStr + text.substring(index + 1);
    }

    /**
     * 将String类型数组转换成int类型数组
     *
     * @param strArray
     * @return
     */
    public static int[] converStringArrayToIntArray(String[] strArray) {
        if (strArray.length > 0) {
            int[] arr = new int[strArray.length];
            for (int i = 0, len = strArray.length; i < len; i++) {
                arr[i] = Integer.parseInt(strArray[i]);
            }
            return arr;
        }
        return null;
    }
}
