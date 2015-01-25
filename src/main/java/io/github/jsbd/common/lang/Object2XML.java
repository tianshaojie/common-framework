package io.github.jsbd.common.lang;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

public class Object2XML {

    /**
     * 对象输出到XML文件
     *
     * @param obj         待输出的对象
     * @param outFileName 目标XML文件的文件名
     * @return 返回输出XML文件的路径
     * @throws FileNotFoundException
     */
    public static String object2XML(Object obj, String outFileName)
            throws FileNotFoundException {
        // 构造输出XML文件的字节输出流
        File outFile = new File(outFileName);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(outFile));

        // 构造一个XML编码器
        XMLEncoder xmlEncoder = new XMLEncoder(bos);
        // 使用XML编码器写对象
        xmlEncoder.writeObject(obj);
        // 关闭编码器
        xmlEncoder.close();

        return outFile.getAbsolutePath();
    }

    /**
     * 把XML文件解码成对象
     *
     * @param inFileName 输入的XML文件
     * @return 返回生成的对象
     * @throws FileNotFoundException
     */
    public static Object xml2Object(String inFileName)
            throws FileNotFoundException {
        // 构造输入的XML文件的字节输入流
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                inFileName));
        // 构造一个XML解码器
        XMLDecoder xmlDecoder = new XMLDecoder(bis);
        // 使用XML解码器读对象
        Object obj = xmlDecoder.readObject();
        // 关闭解码器
        xmlDecoder.close();

        return obj;
    }
}
