package manager;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PropertiesUtil {

    private static final Properties PROPERTIES = new Properties();
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static {
        LoadProperties();
    }

    private static void LoadProperties() {
        try (var inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("app.properties")) {
            PROPERTIES.load(inputStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[Error] " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private PropertiesUtil() {
    }

}
