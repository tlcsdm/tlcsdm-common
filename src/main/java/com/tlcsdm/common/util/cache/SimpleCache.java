package com.tlcsdm.common.util.cache;

import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易缓存实现
 *
 * @author: TangLiang
 * @date: 2021/9/11 19:46
 * @since: 1.0
 */
public class SimpleCache {

    private static Map<String, Object> map = new ConcurrentHashMap<>();

    public static Object put(@NonNull String key, @NonNull Object value) {
        return map.put(key, value);
    }

    public static Object get(@NonNull String key) {
        return map.get(key);
    }

    public static Object remove(@NonNull String key) {
        return map.remove(key);
    }

    public static void removeAll() {
        map.clear();
    }

}
