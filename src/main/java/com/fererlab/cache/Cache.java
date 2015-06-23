package com.fererlab.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * acm 3/21/13
 */
public class Cache {

    private static ArrayList<String> cacheFileExtensions;
    private static ArrayList<String> cacheDirs;
    private static ArrayList<String> cacheFiles;

    private static ConcurrentHashMap<Object, Object> cacheMap;

    public static Object getValue(Object key) {
        return cacheMap.get(key);
    }

    public static boolean contains(Object key) {
        return cacheMap.containsKey(key);
    }

    public static Object put(String key, Object value) {
        return cacheMap.put(key, value);
    }

    public static void put(Map<Object, Object> map) {
        cacheMap.putAll(map);
    }

    public static void create(Map<Object, Object> map) {
        cacheMap = new ConcurrentHashMap<>();
        cacheFileExtensions = new ArrayList<>();
        cacheDirs = new ArrayList<>();
        cacheFiles = new ArrayList<>();

        for (Object key : map.keySet()) {
            if (String.valueOf(key).startsWith("cache.")) {
                String[] values = String.valueOf(map.get(key)).split(",");
                if ("cache.fileExtensions".equals(key)) {
                    Collections.addAll(cacheFileExtensions, values);
                } else if ("cache.dirs".equals(key)) {
                    Collections.addAll(cacheDirs, values);
                } else if ("cache.files".equals(key)) {
                    Collections.addAll(cacheFiles, values);
                } else {
                    cacheMap.put(key, values);
                }
            } else {
                cacheMap.put(key, map.get(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean putIfCacheable(String key, Object value) {
        if (key != null && value != null) {
            String extension = null;
            String dir = null;
            String[] keySplit = key.split(".");
            if (keySplit.length > 0) {
                extension = keySplit[keySplit.length - 1];
            }
            keySplit = key.split("/");
            if (keySplit.length > 0) {
                dir = key.substring(0, key.length() - keySplit[keySplit.length - 1].length());
            }
            if ((cacheFiles.contains(key) || cacheDirs.contains(dir) || cacheFileExtensions.contains(extension))) {
                cacheMap.put(key, value);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Map.Entry<byte[], String> getContentIfCached(String contentKey) {
        if (cacheMap.containsKey(contentKey) && cacheMap.get(contentKey) instanceof Map.Entry) {
            return (Map.Entry<byte[], String>) cacheMap.get(contentKey);
        } else {
            return null;
        }
    }

}
