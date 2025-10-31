package net.ximatai.muyun.database.core.builder;

import java.util.ArrayList;
import java.util.List;

public class Index {
    private String name;
    private List<String> columns;
    private boolean unique;

    public Index(String columnName, boolean unique) {
        this.columns = new ArrayList<>();
        this.columns.add(columnName);
        this.unique = unique;
    }

    public Index(List<String> columns, boolean unique) {
        this.columns = columns;
        this.unique = unique;
    }

    public boolean isUnique() {
        return unique;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
