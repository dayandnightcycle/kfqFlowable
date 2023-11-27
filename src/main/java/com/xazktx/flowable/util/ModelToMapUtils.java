package com.xazktx.flowable.util;

import lombok.extern.slf4j.Slf4j;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ModelToMapUtils {

    public static Map<String, Object> transBeanToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors()) {
                String key = property.getName();

                if (!key.equals("class")) {

                    Object value = null;
                    try {
                        value = property.getReadMethod().invoke(obj);
                    } catch (NullPointerException e) {
                        //处理空值
                        //value = null;
                    } catch (Exception e) {
                        //忽略
                    }

                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("transBean2Map Error {}", e);
        }

        return map;
    }
}
