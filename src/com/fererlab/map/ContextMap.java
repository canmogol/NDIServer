package com.fererlab.map;

import com.fererlab.ndi.WireContext;

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

    public void readContextMap(URL contextMapFile) {
        try {
            Map<String, String> beans = new HashMap<String, String>();
            Properties properties = new Properties();
            properties.load(new FileReader(contextMapFile.getFile()));
            for (Object key : properties.keySet()) {
                beans.put(String.valueOf(key), String.valueOf(properties.get(key)));
            }
            context = new WireContext(beans);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public WireContext getContext() {
        return context;
    }
}
