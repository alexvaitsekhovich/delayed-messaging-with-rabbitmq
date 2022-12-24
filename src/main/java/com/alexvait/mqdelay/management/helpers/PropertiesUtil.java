package com.alexvait.mqdelay.management.helpers;

import java.io.IOException;
import java.util.Properties;

/**
 * Helper utility for properties.
 */
public class PropertiesUtil {

    public static final String APPLICATION_PROPERTIES_FILE = "/application.properties";

    private static Properties properties;

    /**
     * Using a monostate pattern with data initialization put into a method, instead of a static block.
     */
    public String getProperty(String propertyName) {
        if (properties == null) {
            synchronized (PropertiesUtil.class) {
                try {
                    initProperties();
                } catch (IOException e) {
                    // we cannot work further without the configuration data
                    e.printStackTrace();
                }
            }
        }

        if (propertyName != null && !propertyName.isEmpty()) {
            return properties.getProperty(propertyName);
        }

        return null;
    }

    private void initProperties() throws IOException {
        if (properties == null) {
            properties = new Properties();

            properties.load(PropertiesUtil.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
            properties.keySet().forEach(this::collectProperty);
        }
    }

    private void collectProperty(Object k) {
        String key = k.toString();
        if (System.getProperty(key) != null) {
            properties.put(key, System.getProperty(key));
        }

        // System properties will overwrite the data
        // Environment variables will overwrite the system properties
        if (System.getenv(key) != null) {
            properties.put(key, System.getenv(key));
        }
    }
}
