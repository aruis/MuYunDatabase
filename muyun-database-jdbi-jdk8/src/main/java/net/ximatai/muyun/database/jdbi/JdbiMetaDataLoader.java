package net.ximatai.muyun.database.jdbi;

import net.ximatai.muyun.database.core.IMetaDataLoader;
import net.ximatai.muyun.database.core.exception.MuYunDatabaseException;
import net.ximatai.muyun.database.core.metadata.DBColumn;
import net.ximatai.muyun.database.core.metadata.DBIndex;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBSchema;
import net.ximatai.muyun.database.core.metadata.DBTable;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.ximatai.muyun.database.core.exception.MuYunDatabaseException.Type.READ_METADATA_ERROR;

public class JdbiMetaDataLoader implements IMetaDataLoader {

    private Jdbi jdbi;

    public Jdbi getJdbi() {
        return jdbi;
    }

    public JdbiMetaDataLoader(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public DBInfo getDBInfo() {
        return getJdbi().withHandle(handle -> {
            Connection connection = handle.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();

                DBInfo info = new DBInfo(metaData.getDatabaseProductName());

                String databaseName = connection.getCatalog();
                info.setName(databaseName);

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

                try (ResultSet tablesRs = metaData.getTables(null, "%", "%", new String[]{"TABLE"})) {
                    while (tablesRs.next()) {
                        String tableName = tablesRs.getString("TABLE_NAME");
                        String schema = tablesRs.getString("TABLE_SCHEM");
                        if (schema == null) {
                            schema = databaseName;
                        }

                        DBTable table = new DBTable(this).setName(tableName).setSchema(schema);
                        info.getSchema(schema).addTable(table);
                    }
                }

                return info;
            } catch (Exception e) {
                e.printStackTrace();
                throw new MuYunDatabaseException(e.getMessage(), READ_METADATA_ERROR);
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public List<DBIndex> getIndexList(String schema, String table) {
        List<DBIndex> indexList = new ArrayList<>();
        jdbi.useHandle(handle -> {
            Connection connection = handle.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet rs = metaData.getIndexInfo(null, schema, table, false, false)) {
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
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
                try (ResultSet rs = metaData.getColumns(null, schema, table, null)) {
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
                        column.setDescription(rs.getString("REMARKS"));

                        columnMap.put(column.getName(), column);
                    }
                }

                // Primary keys
                try (ResultSet rs = metaData.getPrimaryKeys(null, schema, table)) {
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
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        return columnMap;
    }
}
