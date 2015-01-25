package io.github.jsbd.common.lang;

import java.util.Map;

public interface Propertyable {
    Object getProperty(String key);

    Map<String, Object> getProperties();

    void setProperty(String key, Object value);

    void setProperties(Map<String, Object> properties);
}
