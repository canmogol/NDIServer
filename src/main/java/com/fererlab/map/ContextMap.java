package com.fererlab.map;

import com.fererlab.ndi.WireContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * acm 3/21/13
 */
public class ContextMap {

    private static ContextMap instance;
    private WireContext context;

    private ContextMap() {
    }

    public static ContextMap getInstance() {
        if (instance == null) {
            instance = new ContextMap();
        }
        return instance;
    }

    public void readContextMap(String directoryName) {
        URL url = getClass().getClassLoader().getResource(directoryName);
        if (url != null) {
            try {
                File contextMapFile = new File(url.getPath() + "/ContextMap.properties");
                Map<String, String> beans = new HashMap<>();
                Properties properties = new Properties();
                properties.load(new FileReader(contextMapFile));
                for (Object key : properties.keySet()) {
                    beans.put(String.valueOf(key), String.valueOf(properties.get(key)));
                }
                context = new WireContext(beans);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public WireContext getContext() {
        return context;
    }
}
