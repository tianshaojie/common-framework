package io.github.jsbd.common.lang;

import java.util.UUID;

public interface Identifiable {
  void setIdentification(UUID id);

  UUID getIdentification();
}
