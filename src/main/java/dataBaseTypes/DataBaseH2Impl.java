package dataBaseTypes;

import annotations.Column;
import annotations.Id;

import java.lang.reflect.Field;

public class DataBaseH2Impl implements DataBase{

    public String createId(Field field, Id id) {
        return (id.value().isEmpty() ? field.getName() : id.value()) + " "
                + convertIntoSQL(field.getType()) + " PRIMARY KEY AUTO_INCREMENT";
    }

    public String convertIntoSQL(Class<?> clazz) {
        return switch (clazz.getSimpleName()) {
            case "Long", "long" -> "BIGINT";
            case "Integer", "int" -> "INTEGER";
            case "String" -> "varchar(250)";
            case "LocalDate" -> "DATE";
            default -> {
                throw new IllegalArgumentException("Unknown type");
            }
        };
    }
}
