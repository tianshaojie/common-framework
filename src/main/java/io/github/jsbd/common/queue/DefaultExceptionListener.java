package io.github.jsbd.common.queue;

import java.util.List;

public class DefaultExceptionListener implements ExceptionListener<String> {

  private FileWriteExecutor fileWriteExecutor;

  @Override
  public void onException(List<String> strings) {
    fileWriteExecutor.execute(strings);
  }

  public void setBackupDir(String backupDir) {
    fileWriteExecutor.setFileDir(backupDir);
  }

  public void setFileName(String fileName) {
    fileWriteExecutor.setFileName(fileName);
  }

  public void setFileWriteExecutor(FileWriteExecutor fileWriteExecutor) {
    this.fileWriteExecutor = fileWriteExecutor;
  }

}
