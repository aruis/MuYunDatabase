package net.ximatai.muyun.database.core.builder;

import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.annotation.AnnotationProcessor;
import net.ximatai.muyun.database.core.exception.MuYunDatabaseException;
import net.ximatai.muyun.database.core.metadata.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.ximatai.muyun.database.core.metadata.DBInfo.Type.MYSQL;
import static net.ximatai.muyun.database.core.metadata.DBInfo.Type.POSTGRESQL;

public class TableBuilder {

    private final DBInfo info;
    private final IDatabaseOperations db;

    public TableBuilder(IDatabaseOperations db) {
        this.db = db;
        this.info = db.getDBInfo();
    }

    public boolean build(Class<?> entityClass) {
        TableWrapper wrapper = AnnotationProcessor.fromEntityClass(entityClass);
        return build(wrapper);
    }

    public boolean build(TableWrapper wrapper) {
        boolean result = false;
        String schema = wrapper.getSchema();
        String name = wrapper.getName();

        if (schema == null) {
            schema = info.getDefaultSchemaName();
        }

        if (info.getSchema(schema) == null) {
            db.execute("create schema if not exists " + schema);
            info.addSchema(new DBSchema(schema));
        }

        List<TableBase> inherits = wrapper.getInherits();

        if (inherits != null && !inherits.isEmpty()) {
            inherits.forEach(inherit -> {
                if (!info.getSchema(inherit.getSchema()).containsTable(inherit.getName())) {
                    throw new MuYunDatabaseException("Table " + inherit + " does not exist");
                }
            });
        }

        if (!info.getSchema(schema).containsTable(wrapper.getName())) {
            db.execute("create table " + schema + "." + name + "\n" +
                    " (\n" +
                    "  a_temp_column int\n" +
                    " ) " + inheritSQL(inherits));

            result = true;
            info.getSchema(schema).addTable(new DBTable(db.getMetaDataLoader()).setName(name).setSchema(schema));
        }

        DBTable dbTable = info.getSchema(schema).getTable(wrapper.getName());

        if (wrapper.getComment() != null) {
            if (getDatabaseType().equals(POSTGRESQL)) {
                db.execute("comment on table " + schema + "." + name + " is '" + wrapper.getComment() + "'");
            } else if (getDatabaseType().equals(MYSQL)) {
                db.execute("alter table " + schema + "." + name + " comment '" + wrapper.getComment() + "'");
            }
        }

        if (wrapper.getPrimaryKey() != null) {
            checkAndBuildColumn(dbTable, wrapper.getPrimaryKey());
            dbTable.resetColumns();
        }

        wrapper.getColumns().forEach(column -> {
            checkAndBuildColumn(dbTable, column);
        });

        if (result) {
            db.execute("alter table " + schema + "." + name + " drop column a_temp_column;");
        }

        dbTable.resetColumns();

        wrapper.getIndexes().forEach(index -> {
            checkAndBuildIndex(dbTable, index);
        });

        dbTable.resetIndexes();

        return result;

    }

    private boolean checkAndBuildColumn(DBTable dbTable, Column column) {
        boolean result = false;
        String name = column.getName();
        ColumnType dataType = column.getType();
        String type = getColumnTypeTransform().transform(dataType);

        if (ColumnType.UNKNOWN.name().equals(type)) {
            throw new MuYunDatabaseException("column: " + column + " type not provided");
        } else {
            Objects.requireNonNull(type, "column: " + column + " type not provided");
        }

        Object defaultValue = column.getDefaultValue();
        String comment = column.getComment();
        String length = getColumnLength(column);

        boolean sequence = column.isSequence();
        boolean nullable = column.isNullable();
        boolean primaryKey = column.isPrimaryKey();

        if (!dbTable.contains(name)) {
            db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " add " + name + " " + type + length);
            dbTable.resetColumns();
            result = true;
        }

        DBColumn dbColumn = dbTable.getColumn(name);

        if (!type.equals(dbColumn.getType()) || column.getLength() != null && !column.getLength().equals(dbColumn.getLength())) {
            if (getDatabaseType().equals(POSTGRESQL)) {
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " alter column " + name + " type " + type + length);
            } else if (getDatabaseType().equals(MYSQL)) {
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " modify column " + name + " " + type + length);
            }
        }

        if (primaryKey && !dbColumn.isPrimaryKey()) {
            db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " add primary key (" + name + ")");
        }

        if (dbColumn.isNullable() != nullable) {
            if (getDatabaseType().equals(POSTGRESQL)) {
                String flag = nullable ? "drop" : "set";
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " alter column " + name + " " + flag + " not null");
            } else if (getDatabaseType().equals(MYSQL)) {
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " modify column " + name + " " + type + " " + (nullable ? "null" : "not null"));
            }

        }

        if (!dbColumn.isSequence() && !Objects.equals(dbColumn.getDefaultValue(), defaultValue)) {
            if ("varchar".equalsIgnoreCase(type) && defaultValue instanceof String) {
                String value = defaultValue.toString();
                if (!value.contains("(") && !value.contains(")")) {
                    defaultValue = "'" + value + "'";
                }
            }

            if (getDatabaseType().equals(POSTGRESQL)) {
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " alter column " + name + " set default " + defaultValue);
            } else if (getDatabaseType().equals(MYSQL)) {
                if ("AUTO_INCREMENT".equals(defaultValue)) {
                    db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " modify column " + name + " " + type + " " + defaultValue);
                } else {
                    db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " modify column " + name + " " + type + length + " DEFAULT " + defaultValue);
                }

            }
        }

        if (comment != null && !Objects.equals(dbColumn.getDescription(), comment)) {
            if (getDatabaseType().equals(POSTGRESQL)) {
                db.execute("comment on column " + dbTable.getSchema() + "." + dbTable.getName() + "." + name + " is '" + comment + "'");
            } else if (getDatabaseType().equals(MYSQL)) {
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " modify column " + name + " " + type + length + " COMMENT '" + comment + "'");
//                ALTER TABLE public.basic modify COLUMN id BIGINT COMMENT '这里是你的注释内容';
            }

        }

        if (getDatabaseType().equals(POSTGRESQL) && dbColumn.isSequence() != sequence) {
            String seq = dbTable.getName() + "_" + name + "_sql";
            if (sequence) {
                db.execute("create sequence if not exists " + dbTable.getSchema() + "." + seq);
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " alter column " + name + " set default nextval('" + dbTable.getSchema() + "." + seq + "')");
            } else {
                db.execute("alter table " + dbTable.getSchema() + "." + dbTable.getName() + " alter column " + name + " drop default");
                db.execute("drop sequence if exists " + dbTable.getSchema() + "." + seq + ";");
            }
        }

        return result;
    }

    private String getColumnLength(Column column) {
        String length = column.getLength() == null ? "" : "(" + column.getLength() + ")";

        if (column.getType().equals(ColumnType.TEXT)) {
            return "";
        }

        if (column.getType().equals(ColumnType.NUMERIC)) {
            if (column.getScale() != null && column.getPrecision() != null) {
                return "(" + column.getPrecision() + "," + column.getScale() + ")";
            }
        }

        return length;
    }

    private boolean checkAndBuildIndex(DBTable dbTable, Index index) {
        List<String> columns = index.getColumns();
        List<DBIndex> indexList = dbTable.getIndexList();
        Optional<DBIndex> dbIndexOptional = indexList.stream().filter(i -> new HashSet<>(i.getColumns()).equals(new HashSet<>(columns))).findFirst();

        if (dbIndexOptional.isPresent()) {
            DBIndex dbIndex = dbIndexOptional.get();
            if (dbIndex.isUnique() == index.isUnique()) {
                return false;
            } else {
                db.execute("drop index " + dbTable.getSchema() + "." + dbIndex.getName() + ";");
            }

        }

        String indexName = dbTable.getName() + "_" + String.join("_", columns) + "_";
        String unique = "";
        String nameSuffix = "index";
        if (index.isUnique()) {
            unique = "unique";
            nameSuffix = "uindex";
        }

        if (getDatabaseType().equals(POSTGRESQL)) {
            db.execute("create " + unique + " index if not exists " + indexName + nameSuffix + " on " + dbTable.getSchema() + "." + dbTable.getName() + "(" + String.join(",", columns) + ");");
        } else if (getDatabaseType().equals(MYSQL)) {
            db.execute("create " + unique + " index " + indexName + nameSuffix + " on " + dbTable.getSchema() + "." + dbTable.getName() + "(" + String.join(",", columns) + ");");
        }

        return true;
    }

    private String inheritSQL(List<TableBase> inherits) {
        if (inherits == null || inherits.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("inherits (");
        inherits.forEach(inherit -> {
            builder.append(inherit.getSchema())
                    .append(".")
                    .append(inherit.getName());
        });
        builder.append(")");
        return builder.toString();
    }

    private DBInfo.Type getDatabaseType() {
        String dbName = info.getTypeName().toUpperCase();
        switch (dbName) {
            case "POSTGRESQL":
                return POSTGRESQL;
            default:
                return MYSQL;
        }
    }

    private IColumnTypeTransform getColumnTypeTransform() {
        switch (getDatabaseType()) {
            case POSTGRESQL:
                return IColumnTypeTransform.POSTGRESQL;
            default:
                return IColumnTypeTransform.DEFAULT;
        }
    }

}
