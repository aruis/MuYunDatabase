package net.ximatai.muyun.database.core.builder;

public enum ColumnType {
    UNKNOWN,
    VARCHAR,
    TEXT,
    INT,
    BIGINT,
    BOOLEAN,
    TIMESTAMP,
    DATE,
    NUMERIC,
    JSON,
    VARCHAR_ARRAY,
    INT_ARRAY;

    static ColumnType autoTypeWithColumnName(String name) {
        if ("id".equals(name)) {
            return ColumnType.VARCHAR;
        } else if ("pid".equals(name)) {
            return ColumnType.VARCHAR;
        } else if (name.startsWith("v_")) {
            return ColumnType.VARCHAR;
        } else if (name.startsWith("i_")) {
            return ColumnType.INT;
        } else if (name.startsWith("b_")) {
            return ColumnType.BOOLEAN;
        } else if (name.startsWith("t_")) {
            return ColumnType.TIMESTAMP;
        } else if (name.startsWith("d_")) {
            return ColumnType.DATE;
        } else if (name.startsWith("n_")) {
            return ColumnType.NUMERIC;
        } else if (name.startsWith("id_")) {
            return ColumnType.VARCHAR;
        } else if (name.startsWith("j_")) {
            return ColumnType.JSON;
        } else if (name.startsWith("dict_")) {
            return ColumnType.VARCHAR;
        } else if (name.startsWith("file_")) {
            return ColumnType.VARCHAR;
        } else if (name.startsWith("files_")) {
            return ColumnType.VARCHAR_ARRAY;
        } else if (name.startsWith("ids_")) {
            return ColumnType.VARCHAR_ARRAY;
        } else if (name.startsWith("dicts_")) {
            return ColumnType.VARCHAR_ARRAY;
        }

        return ColumnType.UNKNOWN;
    }
}
