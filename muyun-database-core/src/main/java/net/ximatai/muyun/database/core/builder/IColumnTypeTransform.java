package net.ximatai.muyun.database.core.builder;

public interface IColumnTypeTransform {

    IColumnTypeTransform DEFAULT = Enum::name;

    IColumnTypeTransform POSTGRESQL = type -> {
        switch (type) {
            case VARCHAR_ARRAY:
                return "varchar[]";
            case INT_ARRAY:
                return "int[]";
            case JSON:
                return "jsonb";
            default:
                return type.name();
        }
    };

    String transform(ColumnType type);

}
