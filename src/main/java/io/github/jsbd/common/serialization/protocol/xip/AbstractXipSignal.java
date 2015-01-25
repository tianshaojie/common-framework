package io.github.jsbd.common.serialization.protocol.xip;

import io.github.jsbd.common.lang.DefaultPropertiesSupport;

import java.util.UUID;

public class AbstractXipSignal extends DefaultPropertiesSupport implements XipSignal {

  private UUID uuid = UUID.randomUUID();

  @Override
  public void setIdentification(UUID id) {
    this.uuid = id;
  }

  @Override
  public UUID getIdentification() {
    return this.uuid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractXipSignal other = (AbstractXipSignal) obj;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }
}
