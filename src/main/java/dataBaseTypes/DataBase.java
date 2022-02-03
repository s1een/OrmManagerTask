package dataBaseTypes;

import annotations.Column;
import annotations.Id;

import java.lang.reflect.Field;

public interface DataBase {
    String createId(Field field, Id id);
    default String createColumn(Field field, Column column) {
        String result = (column.value().isEmpty() ?
                field.getName() : column.value()) + " " + convertIntoSQL(field.getType());
        if (column.allowNull()) {
            result += " NOT NULL";
        }
        return result;
    }
    String convertIntoSQL(Class<?> clazz);
}
