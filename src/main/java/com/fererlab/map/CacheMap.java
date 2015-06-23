package com.fererlab.map;

import com.fererlab.cache.Cache;

import java.io.File;
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

    public void readCacheMap(String directoryName) {
        URL url = getClass().getClassLoader().getResource(directoryName);
        if (url != null) {
            try {
                File cacheMapFile = new File(url.getPath() + "/CacheMap.properties");
                Properties properties = new Properties();
                properties.load(new FileReader(cacheMapFile));
                Cache.create(properties);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
