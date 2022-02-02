package manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    private ConnectionManager() {
    }

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
            System.out.println("Failed to connect to database " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
