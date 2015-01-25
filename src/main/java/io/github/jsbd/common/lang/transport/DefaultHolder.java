package io.github.jsbd.common.lang.transport;

import io.github.jsbd.common.lang.Holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultHolder implements Holder {

    private Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();

    @Override
    public void put(Object key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object getAndRemove(Object key) {
        Object ret = map.get(key);
        map.remove(key);
        return ret;
    }

    @Override
    public void remove(Object key) {
        map.remove(key);
    }

}
