package net.ximatai.muyun.database.core.metadata;

import java.util.HashSet;
import java.util.Set;

public class DBInfo {
    public enum Type {
        POSTGRESQL, MYSQL
    }

    private String typeName;
    private String name;

    private Set<DBSchema> schemas = new HashSet<>();

    public DBInfo(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public DBInfo setName(String name) {
        this.name = name;
        return this;
    }

    public DBInfo addSchema(DBSchema schema) {
        this.schemas.add(schema);
        return this;
    }

    public String getDefaultSchemaName() {
        return getDatabaseType() == Type.POSTGRESQL ? "public" : name;
    }

    public DBSchema getDefaultSchema() {
        return getSchema(getDefaultSchemaName());
    }

    public DBSchema getSchema(String schemaName) {
        return schemas.stream()
                .filter(schema -> schemaName.equals(schema.getName()))
                .findFirst()
                .orElse(null);
    }

    public String getTypeName() {
        return typeName;
    }

    private Type getDatabaseType() {
        String dbName = getTypeName().toUpperCase();
        switch (dbName) {
            case "POSTGRESQL":
                return Type.POSTGRESQL;
            default:
                return Type.MYSQL;
        }
    }

}
