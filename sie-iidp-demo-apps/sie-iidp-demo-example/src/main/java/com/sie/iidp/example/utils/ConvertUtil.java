package com.sie.iidp.example.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象转换工具类
 *
 * @author cxh
 * @date 2024年03月27日 13:49
 */
public class ConvertUtil {

    /**
     * 对象转Map
     *
     * @param obj
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public static <T> Map<String, Object> convertObjectToMap(T obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Object> resultMap = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            String key = field.getName();
            Object value = field.get(obj);
            resultMap.put(key, value);
        }
        return resultMap;
    }
}
