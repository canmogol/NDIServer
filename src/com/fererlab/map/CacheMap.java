package com.fererlab.map;

import com.fererlab.cache.Cache;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * acm 3/21/13
 */
public class CacheMap {

    private static CacheMap instance;

    private CacheMap() {
    }

    public static CacheMap getInstance() {
        if (instance == null) {
            instance = new CacheMap();
        }
        return instance;
    }

    public void readCacheMap(URL cacheMapFile) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(cacheMapFile.getFile()));
            Cache.create(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
