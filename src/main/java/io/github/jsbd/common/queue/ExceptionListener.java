package io.github.jsbd.common.queue;

import java.util.List;

public interface ExceptionListener<T> {
  void onException(List<T> records);
}
