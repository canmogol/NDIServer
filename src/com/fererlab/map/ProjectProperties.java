package com.fererlab.map;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

/**
 * acm 3/21/13
 */
public class ProjectProperties extends HashMap<String, String> {

    private static ProjectProperties instance;

    private ProjectProperties() {
    }

    public static ProjectProperties getInstance() {
        if (instance == null) {
            instance = new ProjectProperties();
        }
        return instance;
    }

    public void readProjectProperties(String directoryName, String suffix) {
        try {
            URL url = getClass().getClassLoader().getResource(directoryName);
            if (url != null) {
                // first load the project.properties file
                File projectPropertiesFile = new File(url.getPath() + "/project.properties");
                Properties properties = new Properties();
                properties.load(new FileReader(projectPropertiesFile));
                for (Object key : properties.keySet()) {
                    put(String.valueOf(key), String.valueOf(properties.get(key)));
                }
                if(suffix != null && !suffix.isEmpty()){
                    // then load the project.SUFFIX.properties file to override the default values
                    projectPropertiesFile = new File(url.getPath() + "/project" + suffix + ".properties");
                    properties = new Properties();
                    properties.load(new FileReader(projectPropertiesFile));
                    for (Object key : properties.keySet()) {
                        put(String.valueOf(key), String.valueOf(properties.get(key)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
