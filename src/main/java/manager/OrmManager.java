package manager;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
    public void delete(Class<?> table, String condition) {
        Entity entity = table.getAnnotation(Entity.class);
        List<String> fields = getFields(table);
        if (where(condition,fields))
        {
            var cmd = "DELETE FROM " + entity.value() + " WHERE " + condition.toUpperCase() + ";";
            runCommand(cmd);
        }
        else System.out.println("Wrong condition format");

    }

    private boolean where(String condition, List<String> fields){
        return checkCondition(condition,fields);
    }

    private boolean checkCondition(String condition, List<String> fields) {
        var conditionSymbol = checkConditionSymbol(condition);
        if(conditionSymbol == "-1")
            return false;
        var position = condition.lastIndexOf(conditionSymbol);
        var columnName= condition.substring(0,position).toUpperCase();
        for (String f : fields) {
            if(Objects.equals(f.toString().toUpperCase() ,columnName))
            {
                return true;
            }
        }
        return false;
    }

    private String checkConditionSymbol(String condition){
        String[] symbols = {};
        symbols = new String[]{"=",">",">=","<","<=","<>"," LIKE ", " IN "};
        for (String s : symbols)
            if(condition.lastIndexOf(s) != -1)
                return s;
        return "-1";
    }

    private List<String> getFields(Class<?> table){
        Entity entity = table.getAnnotation(Entity.class);
        List<String> fields = new ArrayList<>();
        if (entity == null) {
            System.out.println("No table annotations for class " + table.getName());
        } else {
            for (Field field : table.getDeclaredFields()) {
                Annotation[] annotations = field.getDeclaredAnnotations();

                for (var annotation:annotations) {
                    if (annotation instanceof Id) {
                        fields.add(readAnnotation(field,(Id)annotation));
                    }
                    if(annotation instanceof Column) {
                        fields.add(readAnnotationColumn(field,(Column)annotation));
                    }
                }
            }
        }
        return fields;
    }

    private String readAnnotation(Field field, Id id) {

        return (Objects.equals((id).value(), "") ? field.getName() : id.value()).toUpperCase();
    }

    private String readAnnotationColumn(Field field, Column column) {
        return (Objects.equals((column).value(), "") ? field.getName() : column.value()).toUpperCase();
    }

    private static String removeLastChar(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 1))
                .orElse(s);
    }
}
