package manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionManager {
    private ConnectionManager() {
    }

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String USERNAME_KEY = ".username";
    private static final String PASSWORD_KEY = ".password";
    private static final String URL = ".url";

    public static Connection open(String key) {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(key + URL),
                    PropertiesUtil.get(key + USERNAME_KEY),
                    PropertiesUtil.get(key + PASSWORD_KEY)
            );
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[Error] " + e.getMessage());
            System.out.println("Failed to connect to database " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
