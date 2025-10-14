package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Table;

import java.sql.Timestamp;

@Table(name = "test_entity")
public class TestEntityBase {

    @Column(length = 20, comment = "名称", defaultValue = "test")
    public String name;

    @Column(comment = "年龄")
    public int age;

    @Column(precision = 10, scale = 2)
    public double price;

    @Column
    public boolean flag;

    public Timestamp created;

}
