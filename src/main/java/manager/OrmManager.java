package manager;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrmManager {
    Connection connection;
    private static String dbKey;
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    UnaryOperator<String> wrapInQuotes = s -> "'" + s + "'";

    public OrmManager(String database) {
        logger.log(Level.INFO,"[Status] Trying to connect to " + database);
        connection = ConnectionManager.open(database);
    }

    public static OrmManager get(String key) {
        dbKey = key;
        return new OrmManager(key);
    }

    public void prepareRepositoryFor(Class<?> table) {
        Entity entity = table.getAnnotation(Entity.class);
        if (entity == null) {
            logger.log(Level.SEVERE,"[Entity Error] Obtained class without entity annotation");
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
            default -> {
                logger.log(Level.SEVERE,"[Entity Error] Unknown type");
                throw new IllegalArgumentException("Unknown type");
            }
        };
    }

    private static void runCommand(String command) {
        try (var connection = ConnectionManager.open(dbKey); Statement statement = connection.createStatement()) {
            logger.info("[Status] Connected to Data Base.");
            statement.executeUpdate(command);
            logger.log(Level.INFO, "[Executing query] " + command);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"[SQL Error] " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public <T> int update(T object) {
        checkForEntityAnnotation(object.getClass());
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(updateCommandBuilder(object));
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"[SQL Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public <T> String updateCommandBuilder(T object) {
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(readEntityAnnotation(object.getClass(), object.getClass().getAnnotation(Entity.class)))
                .append(" SET ");
        for (var field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                builder.append(readAnnotationColumn(field, field.getAnnotation(Column.class))).append("=");
                try {
                    var o = field.get(object);
                    if (o instanceof String) {
                        builder.append(wrapInQuotes.apply(o.toString())).append(",");
                    } else {
                        builder.append(o.toString()).append(",");
                    }
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE,"[Error] " + e.getMessage());
                    throw new RuntimeException();
                }
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        Field primaryKey = getIdField(object);
        builder.append(" WHERE ")
                .append(readAnnotation(primaryKey, primaryKey.getAnnotation(Id.class)))
                .append(" = ");
        try {
            var id = object.getClass().getDeclaredField(primaryKey.getName());
            id.setAccessible(true);
            builder.append(Objects.toString(id.get(object))).append(";");
        } catch (Exception e) {
            logger.log(Level.SEVERE,"[Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return builder.toString();
    }

    public <T> List<T> getAll(Class<T> clazz) {
        checkForEntityAnnotation(clazz);
        List<T> resultList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            logger.log(Level.INFO,"[Executing query] " + getAllCommandBuilder(clazz));
            ResultSet resultSet = statement.executeQuery(getAllCommandBuilder(clazz));
            while (resultSet.next()) {
                resultList.add(resultSetHandling(resultSet, clazz));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"[SQL Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return resultList;
    }

    public <T> String getAllCommandBuilder(Class<T> clazz){
        return  "SELECT * FROM " + readEntityAnnotation(clazz, clazz.getAnnotation(Entity.class)) + ";";
    }

    private <T> T resultSetHandling(ResultSet resultSet, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
            var fields = object.getClass().getDeclaredFields();

            for (var field : fields) {
                field.setAccessible(true);
                if (field.getType() == String.class) {
                    field.set(object, resultSet.getString(readAnnotationColumn(field, field.getAnnotation(Column.class))));
                } else if (field.isAnnotationPresent(Id.class) || field.getType() == Long.class) {
                    field.set(object, Long.valueOf(resultSet.getInt(readAnnotation(field, field.getAnnotation(Id.class)))));
                } else if (field.getType() == int.class) {
                    field.set(object, resultSet.getInt(readAnnotationColumn(field, field.getAnnotation(Column.class))));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"[SQL Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (InstantiationException | IllegalAccessException e) {
            logger.log(Level.SEVERE,"[Error] " + e.getMessage());
            e.printStackTrace();
        }
        return object;
    }

    public <T> void save(T object) {
        checkForEntityAnnotation(object.getClass());
        try {
            Field primaryKey = getIdField(object);
            primaryKey.setAccessible(true);
            var cmd = insertCommandBuilder(object);
            logger.log(Level.INFO,"[Executing query] " + cmd);
            primaryKey.set(object, getAutoGeneratedIdfromDB(cmd));

        } catch (Exception e) {
            logger.log(Level.SEVERE,"[Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public <T> String insertCommandBuilder(T object){
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(readEntityAnnotation(object.getClass(), object.getClass().getAnnotation(Entity.class)))
                .append("(");
        var clazz = object.getClass();
        List<Object> values = new ArrayList<>();
        for (var field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                builder.append(readAnnotationColumn(field, field.getAnnotation(Column.class)))
                        .append(",");
                try {
                    values.add(field.get(object));
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE,"[Error] " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        builder.deleteCharAt(builder.length() - 1).append(") VALUES (");
        for (var value : values) {
            if (value instanceof String) {
                builder.append(wrapInQuotes.apply(value.toString())).append(",");
            } else {
                builder.append(value.toString()).append(",");
            }
        }
        return builder.deleteCharAt(builder.length() - 1)
                .append(");")
                .toString();
    }

    private long getAutoGeneratedIdfromDB(String command) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(command, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();

            String key;
            if (resultSet.next()) {
                key = resultSet.getString(1);
                return Long.parseLong(key);
            } else {
                logger.log(Level.SEVERE,"[Error] " + "Auto increment field can't be absent");
                throw new NoSuchElementException("Auto increment field can't be absent");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"[SQL Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private <T> Field getIdField(T object) {
        for (var field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        logger.log(Level.SEVERE,"[Error] " + "Id annotated field wasn't found.");
        throw new RuntimeException("Id annotated field wasn't found.");
    }

    public <T> Optional<T> getById(Class<T> clazz, Long id) {
        checkForEntityAnnotation(clazz);
        Optional<T> result = Optional.empty();
        try (Statement statement = connection.createStatement()) {
            var cmd = getByIdCommandBuilder(clazz, id);
            ResultSet resultSet = statement.executeQuery(cmd);
            logger.log(Level.INFO,"[Executing query] " + cmd);
            while (resultSet.next()) {
                result = Optional.of(resultSetHandling(resultSet, clazz));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"[SQL Error] " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public <T> String getByIdCommandBuilder(Class<T> clazz, Long id){
        StringBuilder builder = new StringBuilder("SELECT * FROM ");
        return builder.append(readEntityAnnotation(clazz, clazz.getAnnotation(Entity.class)))
                .append(" WHERE ID = ")
                .append(id.toString())
                .append(";")
                .toString();
    }

    public void delete(Class<?> table, String condition) {
        Entity entity = table.getAnnotation(Entity.class);
        List<String> fields = getFields(table);
        if (where(condition,fields))
        {
            var cmd = "DELETE FROM " + entity.value() + " WHERE " + condition.toUpperCase() + ";";
            runCommand(cmd);
        }
        else {
            logger.warning("Wrong condition format: " + condition);
            System.out.println("Wrong condition format");
        }

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
            logger.log(Level.WARNING, "No table annotations for class " + table.getName());
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

    private <T> String readEntityAnnotation(Class<T> clazz, Entity entity) {

        return (Objects.equals((entity).value(), "") ? clazz.getSimpleName() : entity.value())
                .toUpperCase();
    }

    private <T> void checkForEntityAnnotation(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            logger.log(Level.SEVERE,"[Error] " + " Obtained class without entity annotation!");
            throw new IllegalArgumentException("Obtained class without entity annotation!");
        }
    }

    private static String removeLastChar(String s) {
        return Optional.ofNullable(s)
                .filter(str -> str.length() != 0)
                .map(str -> str.substring(0, str.length() - 1))
                .orElse(s);
    }
}
