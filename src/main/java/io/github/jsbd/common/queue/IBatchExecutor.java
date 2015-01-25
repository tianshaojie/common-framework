package io.github.jsbd.common.queue;

import java.util.List;

public interface IBatchExecutor<T> {
  void execute(List<T> records);
}
