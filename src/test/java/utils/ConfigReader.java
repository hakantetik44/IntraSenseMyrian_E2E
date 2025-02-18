package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/configuration.properties";

    static {
        try {
            properties = new Properties();
            FileInputStream fis = new FileInputStream(CONFIG_PATH);
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            System.out.println("Configuration file not found at: " + CONFIG_PATH);
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getBrowser() {
        return getProperty("browser");
    }

    public static String getBaseUrl() {
        return getProperty("base_url");
    }

    public static boolean getHeadless() {
        return Boolean.parseBoolean(getProperty("headless"));
    }

    public static int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit_wait"));
    }

    public static int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("page_load_timeout"));
    }
} 