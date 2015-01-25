package io.github.jsbd.common.lang;

/**
 * 通用缓存接口
 */
public interface Cache<K, V> {

    V get(K key);

    boolean put(K key, V value);

    boolean put(K key, V value, int TTL);

    boolean update(K key, V value);

    boolean remove(K key);

    boolean clear();

    void destroy();

    boolean containsKey(K key);

    boolean flushAll();

    boolean isConnected();
}
