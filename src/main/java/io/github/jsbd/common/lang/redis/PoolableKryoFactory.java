package io.github.jsbd.common.lang.redis;

import org.apache.commons.pool.PoolableObjectFactory;

import com.esotericsoftware.kryo.Kryo;

public class PoolableKryoFactory implements PoolableObjectFactory<Kryo> {

  @Override
  public void activateObject(Kryo obj) throws Exception {

  }

  @Override
  public void destroyObject(Kryo obj) throws Exception {
    obj = null;
  }

  @Override
  public Kryo makeObject() throws Exception {
    Kryo kryo = new Kryo();
    return kryo;
  }

  @Override
  public void passivateObject(Kryo obj) throws Exception {
  }

  @Override
  public boolean validateObject(Kryo obj) {
    return true;
  }

}
