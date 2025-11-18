package net.ximatai.muyun.database.core.metadata;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据库信息元数据类
 * 封装数据库的基本信息和模式集合，支持多模式数据库
 */
public class DBInfo {

    /**
     * 数据库类型枚举
     */
    public enum Type {
        POSTGRESQL,  // PostgreSQL数据库
        MYSQL        // MySQL数据库
    }

    private String typeName;           // 数据库类型名称
    private String name;               // 数据库名称
    private Set<DBSchema> schemas = new HashSet<>();  // 数据库模式集合

    /**
     * 构造函数
     *
     * @param typeName 数据库类型名称
     */
    public DBInfo(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    /**
     * 设置数据库名称
     *
     * @param name 数据库名称
     * @return 当前DBInfo实例
     */
    public DBInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 添加数据库模式
     *
     * @param schema 模式对象
     * @return 当前DBInfo实例
     */
    public DBInfo addSchema(DBSchema schema) {
        this.schemas.add(schema);
        return this;
    }

    /**
     * 获取默认模式名称
     * PostgreSQL使用"public"，MySQL使用数据库名称
     *
     * @return 默认模式名称
     */
    public String getDefaultSchemaName() {
        return getDatabaseType() == Type.POSTGRESQL ? "public" : name;
    }

    /**
     * 获取默认模式对象
     *
     * @return 默认模式，不存在时返回null
     */
    public DBSchema getDefaultSchema() {
        return getSchema(getDefaultSchemaName());
    }

    /**
     * 根据名称查找模式
     *
     * @param schemaName 模式名称
     * @return 匹配的模式对象，未找到时返回null
     */
    public DBSchema getSchema(String schemaName) {
        return schemas.stream()
                .filter(schema -> schemaName.equals(schema.getName()))
                .findFirst()
                .orElse(null);
    }

    public Set<DBSchema> getSchemas() {
        return schemas;
    }

    public String getTypeName() {
        return typeName;
    }

    /**
     * 获取数据库类型枚举
     * 根据类型名称转换为对应的Type枚举
     *
     * @return 数据库类型枚举
     */
    public Type getDatabaseType() {
        String dbName = getTypeName().toUpperCase();
        switch (dbName) {
            case "MYSQL":
                return Type.MYSQL;  // 默认为MySQL类型
            default:
                return Type.POSTGRESQL;
        }
    }
}
