package net.ximatai.muyun.database.core;

import net.ximatai.muyun.database.core.metadata.DBColumn;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBTable;

import java.sql.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public interface IDatabaseOperations {

    IMetaDataLoader getMetaDataLoader();

    default DBInfo getDBInfo() {
        return getMetaDataLoader().getDBInfo();
    }

    default Map<String, ?> transformDataForDB(DBTable dbTable, Map<String, ?> data) {
        return data;
    }

    default String buildInsertSql(String schema, String tableName, Map<String, ?> params) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        Map<String, DBColumn> columnMap = dbTable.getColumnMap();

        StringJoiner columns = new StringJoiner(", ", "(", ")");
        StringJoiner values = new StringJoiner(", ", "(", ")");
        params.keySet().forEach(key -> {
            if (columnMap.containsKey(key)) {
                columns.add(key);
                values.add(":" + key);
            }
        });

        return "insert into " + schema + "." + tableName + " " + columns + " values " + values;
    }

    default String buildUpdateSql(String schema, String tableName, Map<String, ?> params, String pk) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        Map<String, DBColumn> columnMap = dbTable.getColumnMap();

        StringJoiner setClause = new StringJoiner(", ");
        params.keySet().forEach(key -> {
            if (columnMap.containsKey(key)) {
                setClause.add(key + "=:" + key);
            }
        });

        return "update " + schema + "." + tableName + " set " + setClause + " where " + pk + " = :" + pk;
    }

    default String insertItem(String schema, String tableName, Map<String, ?> params) {
        DBTable table = getDBInfo().getSchema(schema).getTable(tableName);
        Map<String, ?> transformed = transformDataForDB(table, params);
        return this.insert(buildInsertSql(schema, tableName, transformed), transformed, "id", String.class);
    }

    default List<String> insertList(String schema, String tableName, List<? extends Map<String, ?>> list) {
        Objects.requireNonNull(list, "The list must not be null");
        if (list.isEmpty()) {
            throw new IllegalArgumentException("The list must not be empty");
        }

        DBTable table = getDBInfo().getSchema(schema).getTable(tableName);
        List<? extends Map<String, ?>> transformedList = list.stream().map(it -> transformDataForDB(table, it)).collect(Collectors.toList());

        return this.batchInsert(buildInsertSql(schema, tableName, transformedList.get(0)), transformedList, "id", String.class);
    }

    default Integer updateItem(String schema, String tableName, Map<String, ?> params) {
        DBTable table = getDBInfo().getSchema(schema).getTable(tableName);
        Map<String, ?> transformed = transformDataForDB(table, params);
        return this.update(buildUpdateSql(schema, tableName, transformed, "id"), transformed);
    }

    default Integer deleteItem(String schema, String tableName, String id) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        return this.delete("DELETE FROM " + schema + "." + tableName + " WHERE id=:id", Collections.singletonMap("id", id));
    }

    default Map<String, Object> getItem(String schema, String tableName, String id) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        return this.row("SELECT * FROM " + schema + "." + tableName + " WHERE id=:id", Collections.singletonMap("id", id));
    }

    <T> T insert(String sql, Map<String, ?> params, String pk, Class<T> idType);

    <T> List<T> batchInsert(String sql, List<? extends Map<String, ?>> paramsList, String pk, Class<T> idType);

    default Map<String, Object> row(String sql, Object... params) {
        return this.row(sql, Arrays.stream(params).collect(Collectors.toList()));
    }

    Map<String, Object> row(String sql, List<?> params);

    Map<String, Object> row(String sql, Map<String, ?> params);

    default Map<String, Object> row(String sql) {
        return row(sql, Collections.emptyList());
    }

    List<Map<String, Object>> query(String sql, Map<String, ?> params);

    List<Map<String, Object>> query(String sql, List<?> params);

    default List<Map<String, Object>> query(String sql, Object... params) {
        return this.query(sql, Arrays.stream(params).collect(Collectors.toList()));
    }

    default List<Map<String, Object>> query(String sql) {
        return this.query(sql, Collections.emptyList());
    }

    Integer update(String sql, Map<String, ?> params);

    default Integer update(String sql, Object... params) {
        return this.update(sql, Arrays.stream(params).collect(Collectors.toList()));
    }

    Integer update(String sql, List<?> params);

    default Integer delete(String sql, Map<String, ?> params) {
        return this.update(sql, params);
    }

    default Integer delete(String sql, Object... params) {
        return this.update(sql, params);
    }

    default Integer delete(String sql, List<?> params) {
        return this.update(sql, params);
    }

    Integer execute(String sql);

    Integer execute(String sql, Object... params);

    Integer execute(String sql, List<?> params);

    Array createArray(List list, String type);
}
