package manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class OrmManager {
    Connection connection;
    private static String dbKey;
    public OrmManager(String database) {
        connection = ConnectionManager.open(database);
        runCommand();
    }

    public static OrmManager get(String key) {
        dbKey = key;
        return new OrmManager(key);
    }

    private static void runCommand() {
        try (var connection = ConnectionManager.open(dbKey); Statement statement = connection.createStatement()) {
            System.out.println("Connected to database " + connection.getTransactionIsolation());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
