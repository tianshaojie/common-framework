package io.github.jsbd.common.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapUtil {

  private static final Logger logger = LoggerFactory.getLogger(MapUtil.class);

  /**
   * fieldName 作为Map的一个key，getFieldName()的返回结果作为Map的值
   *
   * @param object
   *          参数对象
   * @return Map 返回经过解析的参数对象
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Map objectToMap(Object object) {
    if (object == null) {
      return new HashMap();
    }

    if (object instanceof Map) {
      return (Map) object;
    }

    Map map = new HashMap();

    /**
     * IBatis的默认对象参数名
     */
    map.put("value", object);
    Field[] fields = object.getClass().getDeclaredFields();
    for (Field field : fields) {
      String fieldName = field.getName();
      field.setAccessible(true);
      try {
        map.put(fieldName, field.get(object));
      } catch (Exception e) {
        logger.error("objectToMap error: ", e);
      }
    }

    return map;
  }

  /**
   * @param bean
   * @param parameters
   * @throws Exception
   */
  @SuppressWarnings({ "rawtypes" })
  public static void populate(Object bean, Map parameters) {
    for (Iterator i = parameters.entrySet().iterator(); i.hasNext();) {
      Map.Entry entry = (Map.Entry) i.next();
      Object key = entry.getKey();
      Object value = entry.getValue();
      String strKey;

      if (key != null) {
        if (!(key instanceof String)) {
          strKey = key.toString();
        } else {
          strKey = (String) key;
        }

        FieldUtil.setFieldValue(bean, strKey, value);
      }
    }
  }
}
