package com.fererlab.map;

import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

/**
 * acm 3/14/13
 */
public class MimeTypeMap extends HashMap<String, String> {

    private static MimeTypeMap instance;

    private MimeTypeMap() {
    }

    public static MimeTypeMap getInstance() {
        if (instance == null) {
            instance = new MimeTypeMap();
        }
        return instance;
    }

    public void readMimeTypeMap(URL mimeTypeMapFile) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(mimeTypeMapFile.getFile()));
            for (Object key : properties.keySet()) {
                String valueString = String.valueOf(properties.get(key));
                String[] values = valueString.split(" ");
                for (String value : values) {
                    this.put(value.trim(), String.valueOf(key).trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String get(Object key) {
        if (containsKey(key)) {
            return super.get(key);
        } else {
            return "";
        }
    }
}
