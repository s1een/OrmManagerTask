package dataBaseTypes;

import annotations.Column;
import annotations.Id;

import java.lang.reflect.Field;

public class DataBasePostgresImpl implements DataBase{

    public String createId(Field field, Id id) {
        return (id.value().isEmpty() ? field.getName() : id.value()) + " "
                + "bigserial" + " PRIMARY KEY";
    }


    public String convertIntoSQL(Class<?> clazz) {
        return switch (clazz.getSimpleName()) {
            case "Long", "long" -> "bigint";
            case "Integer", "int" -> "serial";
            case "String" -> "varchar(250)";
            case "LocalDate" -> "date";
            default -> {
                throw new IllegalArgumentException("Unknown type");
            }
        };
    }
}
