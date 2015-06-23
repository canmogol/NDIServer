package com.fererlab.map;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * acm 3/21/13
 */
public class MessageProperties extends HashMap<String, Map<String, String>> {

    private static MessageProperties instance;
    private String defaultLocale;

    private MessageProperties() {
    }

    public static MessageProperties getInstance() {
        if (instance == null) {
            instance = new MessageProperties();
        }
        return instance;
    }


    public void readMessageProperties(String directoryName) {
        try {
            defaultLocale = Locale.getDefault().toString();
            if (defaultLocale == null || defaultLocale.length() < 2) {
                defaultLocale = "en";
            } else if (defaultLocale.length() >= 2) {
                defaultLocale = defaultLocale.substring(0, 2);
            }

            URL url = getClass().getClassLoader().getResource(directoryName);
            if (url != null) {
                File directory = new File(url.getPath());
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file != null && file.getName().startsWith("message_") && file.getName().endsWith(".properties")) {
                                String locale = file.getName().substring("message_".length(), file.getName().length() - ".properties".length());
                                if (!containsKey(locale)) {
                                    put(locale, new HashMap<String, String>());
                                }
                                Properties properties = new Properties();
                                properties.load(new FileReader(file));
                                for (Object key : properties.keySet()) {
                                    get(locale).put(String.valueOf(key), String.valueOf(properties.get(key)));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getValue(String key) {
        if (containsKey(defaultLocale)) {
            return getValue(defaultLocale, key);
        } else {
            return null;
        }
    }

    public String getValue(Locale locale, String key) {
        return getValue(locale.toString(), key);
    }

    public String getValue(String locale, String key) {
        return super.get(locale).get(key);
    }
}
