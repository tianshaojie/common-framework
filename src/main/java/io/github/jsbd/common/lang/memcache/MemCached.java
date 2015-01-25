package io.github.jsbd.common.lang.memcache;

import io.github.jsbd.common.lang.Cache;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemCached<K, V> implements Cache<K, V> {
  private static Logger   logger              = LoggerFactory.getLogger(MemCached.class);

  private MemCachedClient mc                  = null;
  private String[]        serverList;
  private String          servers;
  private Integer[]       weightList;
  private String          weights;
  private int             initialConnections  = 10;
  private int             minSpareConnections = 5;
  private int             maxSpareConnections = 50;

  private long            maintThreadSleep    = 100000L;
  private int             timeToLive          = 86400;
  private boolean         isConnected         = false;
  private String          cacheRegion;

  public MemCached() {
    this.cacheRegion = "default";
  }

  public void init() {
    initPool();
  }

  public void initPool() {
    initPool(this.cacheRegion);
  }

  public void initPool(String poolName) {
    SockIOPool pool = SockIOPool.getInstance(poolName);

    pool.setServers(this.serverList);
    pool.setWeights(this.weightList);
    pool.setInitConn(this.initialConnections);
    pool.setMinConn(this.minSpareConnections);
    pool.setMaxConn(this.maxSpareConnections);
    pool.setMaintSleep(this.maintThreadSleep);
    pool.setHashingAlg(SockIOPool.CONSISTENT_HASH);
    pool.setFailover(false);
    pool.setFailback(false);
    pool.setAliveCheck(true);
    try {
      pool.initialize();
    } catch (Exception ex) {
      logger.error(">>>> MemCached initialize conntection pool failure.", ex);
      System.exit(0);
    }

    if (pool.isInitialized()) {
      boolean isConnectSuccess = false;
      for (String host : this.serverList) {
        SockIOPool.SockIO io = pool.getConnection(host);
        if (io != null) {
          isConnectSuccess = true;
          break;
        }
      }

      if (isConnectSuccess) {
        this.isConnected = true;
        logger.info(">>>> Initialize MemCached [{}] completed.", getCacheRegion());
      } else {
        logger.warn(">>>> Create memCached sockIO failure. cacheRegion=[{}]", getCacheRegion());
      }
    } else {
      logger.warn(">>>> Initialize memCached [{}] failure.", getCacheRegion());
    }

    if (this.mc == null) {
      this.mc = new MemCachedClient(poolName);
    }
  }

  @SuppressWarnings("unchecked")
  public V get(K key) {
    if (!isConnected()) {
      logger.warn("Memcached not initialized, region=[{}]", getCacheRegion());
      return null;
    }

    if (key == null) {
      return null;
    }
    V result = (V) this.mc.get(key.toString());
    if (result != null) {
      return result;
    }
    return null;
  }

  public boolean put(K key, V value) {
    if (!isConnected()) {
      logger.warn("Memcached not initialized, region=[{}]", getCacheRegion());
      return false;
    }

    return put(key, value, this.timeToLive);
  }

  public boolean put(K key, V value, int TTL) {
    if (!isConnected()) {
      logger.warn("Memcached not initialized, region=[{}]", getCacheRegion());
      return false;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(13, TTL);
    return this.mc.set(key.toString(), value, calendar.getTime());
  }

  public boolean update(K key, V value) {
    if (!isConnected()) {
      logger.warn("Memcached not initialized, region=[{}]", getCacheRegion());
      return false;
    }

    return put(key, value);
  }

  public boolean remove(K key) {
    boolean isSuccess = false;
    if (!isConnected()) {
      logger.warn("Memcached not initialized, region=[{}]", getCacheRegion());
      return isSuccess;
    }

    isSuccess = this.mc.delete(key.toString());
    return isSuccess;
  }

  public boolean clear() {
    if (!isConnected()) {
      logger.warn("Memcached not initialized, region=[{}]", getCacheRegion());
      return false;
    }

    return this.mc.flushAll();
  }

  public void destroy() {
    SockIOPool.getInstance(this.cacheRegion).shutDown();
    this.mc = null;
  }

  public boolean containsKey(K key) {
    return get(key) != null;
  }

  @SuppressWarnings("rawtypes")
  public long getSizeInMemory() {
    return Long.valueOf(((Map) this.mc.stats().get("bytes_written")).toString()).longValue();
  }

  @SuppressWarnings("rawtypes")
  public long getElementCountInMemory() {
    return Long.valueOf(((Map) this.mc.stats().get("total_items")).toString()).longValue();
  }

  public long incr(Object key) {
    return this.mc.incr((String) key);
  }

  public long incr(Object key, long inc) {
    return this.mc.incr((String) key, inc);
  }

  public boolean storeCounter(Object key, long counter) {
    return this.mc.storeCounter((String) key, counter);
  }

  public long getCounter(Object key) {
    return this.mc.getCounter((String) key);
  }

  public boolean flushAll() {
    try {
      return this.mc.flushAll();
    } catch (Exception e) {
      logger.error("", e);
    }
    return false;
  }

  public void setInitialConnections(int initialConnections) {
    this.initialConnections = initialConnections;
  }

  public void setMinSpareConnections(int minSpareConnections) {
    this.minSpareConnections = minSpareConnections;
  }

  public void setMaxSpareConnections(int maxSpareConnections) {
    this.maxSpareConnections = maxSpareConnections;
  }

  public void setMaintThreadSleep(long maintThreadSleep) {
    this.maintThreadSleep = maintThreadSleep;
  }

  public void setTimeToLive(int timeToLive) {
    this.timeToLive = timeToLive;
  }

  public int getTimeToLive() {
    return this.timeToLive;
  }

  public void setServers(String servers) {
    this.servers = servers;
    try {
      if (this.servers.indexOf("/") == -1)
        this.serverList = new String[] { this.servers };
      else
        this.serverList = this.servers.split("/");
    } catch (Exception ex) {
      logger.error(">>>> config occurs error. (memcached servers ParseException)", ex);
      System.exit(0);
    }
  }

  public void setWeights(String weights) {
    this.weights = weights;
    try {
      if (this.weights.indexOf("/") == -1) {
        this.weightList = new Integer[] { Integer.valueOf(this.weights) };
      } else {
        String[] ws = this.weights.split("/");
        this.weightList = new Integer[ws.length];
        for (int i = 0; i < ws.length; i++)
          this.weightList[i] = Integer.valueOf(ws[i]);
      }
    } catch (Exception ex) {
      logger.error(">>>> config occurs error. (memcached weights ParseException)", ex);
      System.exit(0);
    }
  }

  public String getCacheRegion() {
    return this.cacheRegion;
  }

  public void setCacheRegion(String cacheRegion) {
    this.cacheRegion = cacheRegion;
  }

  public boolean isConnected() {
    return this.isConnected;
  }
}