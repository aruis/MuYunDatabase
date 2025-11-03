package net.ximatai.muyun.database.jdbi;

import net.ximatai.muyun.database.core.IMetaDataLoader;
import net.ximatai.muyun.database.core.exception.MuYunDatabaseException;
import net.ximatai.muyun.database.core.metadata.*;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static net.ximatai.muyun.database.core.exception.MuYunDatabaseException.Type.READ_METADATA_ERROR;

public class JdbiMetaDataLoader implements IMetaDataLoader {

    private DBInfo info;

    private Jdbi jdbi;

    public Jdbi getJdbi() {
        return jdbi;
    }

    public JdbiMetaDataLoader(Jdbi jdbi) {
        this.jdbi = jdbi;
        initInfo();
    }

    private void initInfo() {
        info = getJdbi().withHandle(handle -> {
            Connection connection = handle.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();

                DBInfo info = new DBInfo(metaData.getDatabaseProductName());

                String databaseName = connection.getCatalog();
                info.setName(databaseName);

                if (info.getDatabaseType().equals(DBInfo.Type.MYSQL)) { // mysql 没有 schema 的概念，其本质是 database
                    handle.createQuery("show databases;")
                            .attachToHandleForCleanup()
                            .mapToMap()
                            .stream()
                            .forEach(map -> {
                                info.addSchema(new DBSchema((String) map.get("database")));
                            });
                } else {
                    try (ResultSet schemasRs = metaData.getSchemas()) {
                        boolean flag = false;
                        while (schemasRs.next()) {
                            flag = true;
                            info.addSchema(new DBSchema(schemasRs.getString("TABLE_SCHEM")));
                        }

                        if (!flag) {
                            info.addSchema(new DBSchema(databaseName));
                        }
                    }
                }

                for (DBSchema schema : info.getSchemas()) {
                    String catalog = null;
                    String schemaPattern = null;

                    if (info.getDatabaseType().equals(DBInfo.Type.MYSQL)) {
                        catalog = schema.getName();
                    } else {
                        schemaPattern = schema.getName();
                    }

                    try (ResultSet tablesRs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
                        while (tablesRs.next()) {
                            String tableName = tablesRs.getString("TABLE_NAME");
                            String schemaName = schema.getName();
                            DBTable table = new DBTable(this).setName(tableName).setSchema(schemaName);
                            info.getSchema(schemaName).addTable(table);
                        }
                    }
                }

                return info;
            } catch (Exception e) {
                e.printStackTrace();
                throw new MuYunDatabaseException(e.getMessage(), READ_METADATA_ERROR);
            }
        });
    }

    @Override
    public DBInfo getDBInfo() {
        return info;
    }

    @Override
    public List<DBIndex> getIndexList(String schema, String table) {
        List<DBIndex> indexList = new ArrayList<>();
        jdbi.useHandle(handle -> {
            Connection connection = handle.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String catalog = null;
                String schemaPattern = null;
                if (info.getDatabaseType().equals(DBInfo.Type.MYSQL)) {
                    catalog = schema;
                } else {
                    schemaPattern = schema;
                }

                try (ResultSet rs = metaData.getIndexInfo(catalog, schemaPattern, table, false, false)) {
                    while (rs.next()) {
                        String indexName = rs.getString("INDEX_NAME");
                        if (indexName.endsWith("_pkey")) { // 主键索引不参与
                            continue;
                        }

                        String columnName = rs.getString("COLUMN_NAME");
                        Optional<DBIndex> hitIndex = indexList.stream().filter(i -> i.getName().equals(indexName)).findFirst();

                        if (hitIndex.isPresent()) {
                            hitIndex.get().addColumn(columnName);
                        } else {
                            DBIndex index = new DBIndex();
                            index.setName(indexName);
                            index.addColumn(columnName);
                            if (!rs.getBoolean("NON_UNIQUE")) {
                                index.setUnique(true);
                            }
                            indexList.add(index);
                        }

                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return indexList;
    }

    @Override
    public Map<String, DBColumn> getColumnMap(String schema, String table) {
        Map<String, DBColumn> columnMap = new HashMap<>();

        getJdbi().useHandle(handle -> {
            Connection connection = handle.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String catalog = null;
                String schemaPattern = null;
                if (info.getDatabaseType().equals(DBInfo.Type.MYSQL)) {
                    catalog = schema;
                } else {
                    schemaPattern = schema;
                }
                try (ResultSet rs = metaData.getColumns(catalog, schemaPattern, table, null)) {
                    while (rs.next()) {
                        DBColumn column = new DBColumn();
                        column.setName(rs.getString("COLUMN_NAME"));
                        column.setType(rs.getString("TYPE_NAME"));
                        column.setLength(rs.getInt("COLUMN_SIZE"));
                        column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                        String defaultValue = rs.getString("COLUMN_DEF");
                        column.setDefaultValue(defaultValue);
                        if ("YES".equals(rs.getString("IS_AUTOINCREMENT"))) {
                            column.setSequence();
                        }
                        if (defaultValue != null && defaultValue.startsWith("nextval(")) {
                            column.setSequence();
                        }
                        if (column.isSequence() && info.getDatabaseType().equals(DBInfo.Type.MYSQL) && defaultValue == null) {
                            column.setDefaultValue("AUTO_INCREMENT");
                        }

                        column.setDescription(rs.getString("REMARKS"));

                        columnMap.put(column.getName(), column);
                    }
                }

                // Primary keys
                try (ResultSet rs = metaData.getPrimaryKeys(catalog, schemaPattern, table)) {
                    while (rs.next()) {
                        String primaryKeyColumn = rs.getString("COLUMN_NAME");
                        DBColumn column = columnMap.get(primaryKeyColumn);
                        if (column != null) {
                            column.setPrimaryKey(true);
                        }
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        return columnMap;
    }
}
