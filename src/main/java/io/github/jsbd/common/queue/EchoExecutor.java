package io.github.jsbd.common.queue;

import java.util.List;

/**
 * Echo Test
 */
public class EchoExecutor implements IBatchExecutor<String> {

  @Override
  public void execute(List<String> records) {
    for (String s : records) {
      System.out.println(s);
    }
  }

}
