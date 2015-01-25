package io.github.jsbd.common.http.cache;

import io.github.jsbd.common.lang.Holder;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedHolder implements Holder {

  private static final Logger logger = LoggerFactory.getLogger(CachedHolder.class);

  private Cache               cache;

  public void setCache(Cache newCache) {
    this.cache = newCache;

    this.cache.getCacheEventNotificationService().registerListener(new CacheEventListener() {

      public void dispose() {
      }

      public void notifyElementEvicted(Ehcache cache, Element element) {
        if (logger.isTraceEnabled()) {
          logger.trace("notifyElementEvicted:" + element.getObjectValue());
        }
      }

      public void notifyElementExpired(Ehcache cache, Element element) {
        if (logger.isTraceEnabled()) {
          logger.trace("notifyElementExpired:" + element.getObjectValue());
        }
      }

      public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        if (logger.isTraceEnabled()) {
          logger.trace("notifyElementPut:" + element.getObjectValue());
        }
      }

      public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        if (logger.isTraceEnabled()) {
          logger.trace("notifyElementRemoved:" + element.getObjectValue());
        }
      }

      public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        if (logger.isTraceEnabled()) {
          logger.trace("notifyElementUpdated:" + element.getObjectValue());
        }
      }

      public void notifyRemoveAll(Ehcache cache) {
        if (logger.isTraceEnabled()) {
          logger.trace("notifyRemoveAll.");
        }
      }

      public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
      }
    });
  }

  @Override
  public void put(Object key, Object value) {
    cache.put(new Element(key, value));
  }

  @Override
  public Object get(Object key) {
    Element element = cache.get(key);
    if (null != element) {
      return element.getObjectValue();
    }
    return null;
  }

  @Override
  public Object getAndRemove(Object key) {
    Object ret = get(key);
    remove(key);
    return ret;
  }

  @Override
  public void remove(Object key) {
    cache.remove(key);
  }

}
