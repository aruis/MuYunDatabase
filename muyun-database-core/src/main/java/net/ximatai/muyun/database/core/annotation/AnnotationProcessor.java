package net.ximatai.muyun.database.core.annotation;

import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.database.core.builder.PredefinedColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotationProcessor {
    public static List<Field> getAllFields(Class<?> type) {

        List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));

        Class<?> superClass = type.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            List<Field> superFields = getAllFields(superClass);
            for (Field field : superFields) {
                if (fields.stream().noneMatch(f -> f.getName().equals(field.getName()))) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static TableWrapper fromEntityClass(Class<?> entityClass) {

        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Table");
        }

        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        TableWrapper tableWrapper = TableWrapper.withName(tableAnnotation.name());
        if (!tableAnnotation.comment().isEmpty()) {
            tableWrapper.setComment(tableAnnotation.comment());
        }
        if (!tableAnnotation.schema().isEmpty()) {
            tableWrapper.setSchema(tableAnnotation.schema());
        }

        // 处理所有字段
        for (Field field : getAllFields(entityClass)) {
            if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                Id idAnnotation = field.getAnnotation(Id.class);

                if (idAnnotation != null) {
                    if (!idAnnotation.value().equals(PredefinedColumn.Id.CUSTOM)) {
                        tableWrapper.addColumn(idAnnotation.value().toColumn());
                        continue;
                    }
                }

                // 确定列名
                String columnName;
                if (idAnnotation != null && !idAnnotation.name().isEmpty()) {
                    columnName = idAnnotation.name();
                } else if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
                    columnName = columnAnnotation.name();
                } else {
                    columnName = field.getName();
                }

                // 创建列
                net.ximatai.muyun.database.core.builder.Column column = net.ximatai.muyun.database.core.builder.Column.of(columnName);

                // 处理Column注解属性
                if (columnAnnotation != null) {
                    if (columnAnnotation.type().equals(ColumnType.UNKNOWN)) {
                        column.setType(TypeMapper.inferSqlType(field.getType()));
                    } else {
                        column.setType(columnAnnotation.type());
                    }

                    if (columnAnnotation.length() > 0) {
                        column.setLength(columnAnnotation.length());
                    }
                    if (columnAnnotation.precision() > 0) {
                        column.setPrecision(columnAnnotation.precision());
                    }
                    if (columnAnnotation.scale() > 0) {
                        column.setScale(columnAnnotation.scale());
                    }
                    if (!columnAnnotation.comment().isEmpty()) {
                        column.setComment(columnAnnotation.comment());
                    }
                    if (!columnAnnotation.defaultValue().isEmpty()) {
                        column.setDefaultValue(columnAnnotation.defaultValue());
                    }
                }

                // 处理Id注解
                if (idAnnotation != null) {
                    tableWrapper.setPrimaryKey(column);
                }

                // 处理Indexed注解
                if (field.isAnnotationPresent(Indexed.class)) {
                    column.setIndexed();
                }

                tableWrapper.addColumn(column);
            }
        }

        return tableWrapper;
    }
}
