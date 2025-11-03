package net.ximatai.muyun.database.core.metadata;

import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;

import java.util.regex.Pattern;

public class DBColumn {
    private String name;
    private String description;
    private String type;
    private String defaultValue;
    private boolean nullable;
    private boolean primaryKey;
    private boolean sequence;
    private Integer length;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
//        return defaultValue;
        return extractDefaultContent(defaultValue);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isSequence() {
        return sequence;
    }

    public void setSequence() {
        this.sequence = true;
    }

    public void setNullable() {
        this.nullable = true;
    }

    public void setPrimaryKey() {
        this.primaryKey = true;
    }

    public String getLabel() {
        if (getDescription() != null) {
            return getDescription();
        }
        return getName();
    }

    public String extractDefaultContent(String input) {
        if (input == null) {
            return null;
        }

        if (this.getType().equalsIgnoreCase("bit")) {
            if (input.equals("1")) {
                return "true";
            } else if (input.equals("0")) {
                return "false";
            }
        }

        if (this.getType().equalsIgnoreCase("varchar")
                && !input.contains("::")
                && !input.endsWith("()")) {
            return "'" + input + "'";
        }

        return input;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        if (Integer.MAX_VALUE != length) { // 未设置长度的情况下，可能会读取到这个值
            this.length = length;
        }
    }

    public Column toColumn() {
        Column column = Column.of(this.getName());
        column.setComment(this.getDescription());
        column.setType(ColumnType.valueOf(this.getType().toUpperCase()));
        column.setDefaultValue(this.getDefaultValue());
        column.setNullable(this.isNullable());
        column.setPrimaryKey(this.isPrimaryKey());
        column.setSequence(this.isSequence());
        column.setLength(this.getLength());
        return column;
    }
}
