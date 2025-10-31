package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.CompositeIndex;
import net.ximatai.muyun.database.core.annotation.Indexed;
import net.ximatai.muyun.database.core.annotation.Table;

import java.sql.Timestamp;

@Table(name = "test_entity")
@CompositeIndex(columns = {"name", "age"}, unique = true)
@CompositeIndex(columns = {"name", "flag"})
public class TestEntityBase {

    @Column(length = 20, comment = "名称", defaultValue = "test")
    public String name;

    @Indexed(unique = true)
    @Column(comment = "学号")
    public int code;

    @Column(comment = "年龄")
    public int age;

    @Column(precision = 10, scale = 2)
    public double price;

    @Indexed
    @Column
    public boolean flag;

    public Timestamp created;

}
