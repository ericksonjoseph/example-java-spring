
package com.ericksonjoseph.assignment.config;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties prop = new Properties();

    private static Config instance = new Config();

    private Config () {

        try {
            load("./src/main/resources/app.config.properties");
        } catch (Exception e) {
            System.out.println("Unable to init global config");
        }
    }

    public static Config getInstance() {
        return instance;
    }

    public static String get(String param) {
        return prop.getProperty(param);
    }

    public static float getFloat(String param) {
        return Float.parseFloat(get(param));
    }

    private static void load(String file) throws FileNotFoundException, IOException {
        prop.load(new FileInputStream(file));
    }
}
