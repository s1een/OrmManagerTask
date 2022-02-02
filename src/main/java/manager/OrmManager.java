package manager;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

public class OrmManager {
    Connection connection;
    private static String dbKey;

    public OrmManager(String database) {
        connection = ConnectionManager.open(database);
    }

    public static OrmManager get(String key) {
        dbKey = key;
        return new OrmManager(key);
    }

    public void prepareRepositoryFor(Class<?> table) {
        Entity entity = table.getAnnotation(Entity.class);
        if (entity == null) {
            throw new IllegalArgumentException("Obtained class without entity annotation ");
        } else {
            String tableName = entity.value().isEmpty() ?
                    table.getName() : entity.value();
            StringJoiner stringJoiner = new StringJoiner(", ", "(", ");");
            StringBuilder createCommand = new StringBuilder().append("DROP TABLE IF EXISTS ")
                    .append(tableName).append("; CREATE TABLE ").append(tableName);
            String column;
            for (Field field : table.getDeclaredFields()) {
                column = createQuery(field);
                if (!column.isEmpty()) {
                    stringJoiner.add(column);
                }
            }
            String query = createCommand.append(stringJoiner).toString();
            runCommand(query);
        }
    }

    private String createQuery(Field field) {
        StringBuilder stringBuilder = new StringBuilder();
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (field.isAnnotationPresent(Id.class)) {
                stringBuilder.append(createId(field, (Id) annotation));
            } else if (field.isAnnotationPresent(Column.class)) {
                stringBuilder.append(createColumn(field, (Column) annotation));
            }
        }
        return stringBuilder.toString();
    }

    private String createId(Field field, Id id) {
        return (id.value().isEmpty() ? field.getName() : id.value()) + " "
                + convertIntoSQL(field.getType()) + " PRIMARY KEY AUTO_INCREMENT";
    }

    private String createColumn(Field field, Column column) {
        String result = (column.value().isEmpty() ?
                field.getName() : column.value()) + " " + convertIntoSQL(field.getType());
        if (column.allowNull()) {
            result += " NOT NULL";
        }
        return result;
    }

    private String convertIntoSQL(Class<?> clazz) {
        return switch (clazz.getSimpleName()) {
            case "Long", "long" -> "BIGINT";
            case "Integer", "int" -> "INTEGER";
            case "String" -> "varchar(250)";
            case "LocalDate" -> "DATE";
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }

    private static void runCommand(String command) {
        try (var connection = ConnectionManager.open(dbKey); Statement statement = connection.createStatement()) {
            statement.executeUpdate(command);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
