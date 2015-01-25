package io.github.jsbd.common.queue;

import io.github.jsbd.common.lang.DateUtil;
import io.github.jsbd.common.lang.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.List;

public class FileWriteExecutor implements IBatchExecutor<String> {

  private static final Logger logger      = LoggerFactory.getLogger(FileWriteExecutor.class);

  private String              fileDir;
  private String              fileName;

  /**
   * The default maximum file size is 64MB.
   */
  private int                 maxFileSize = 64 * 1024 * 1024;

  @Override
  public void execute(List<String> strings) {
    if (logger.isDebugEnabled()) {
      logger.debug("execute - size=[{}]", strings.size());
    }

    File file = genFile(0);
    if (file != null) {
      writeToFile(file, strings);
    }
  }

  File genFile(int index) {
    String filepath = fileDir + File.separator + DateUtil.formatDate(new Date(), "yyyy-MM-dd") + File.separator + fileName + "." + index;
    File file = new File(filepath);
    if (file.exists()) {
      int fileSize = FileUtil.getFileSize(filepath);
      if (fileSize >= maxFileSize) {
        index = index + 1;
        return genFile(index);
      } else {
        return file;
      }
    } else {
      logger.info("create file [{}]", filepath);
      try {
        return FileUtil.createFile(filepath);
      } catch (Exception e) {
        logger.error(">>>> Create File Exception: ", e);
        return null;
      }
    }
  }

  private void writeToFile(File file, List<String> strings) {
    BufferedWriter bufferedOut = null;
    try {
      FileOutputStream out = new FileOutputStream(file, true);
      bufferedOut = new BufferedWriter(new PrintWriter(out));
      for (String s : strings) {
        bufferedOut.write(s + "\n");
      }
      bufferedOut.flush();
    } catch (Exception e) {
      logger.error(">>>> Excute write Exception: ", e);
    } finally {
      try {
        if (bufferedOut != null)
          bufferedOut.close();
      } catch (IOException e) {
        logger.error(">>>> Clost BufferedWriter Exception:", e);
      }
    }
  }

  public void setMaxFileSize(int maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setFileDir(String fileDir) {
    this.fileDir = fileDir;
  }
}
