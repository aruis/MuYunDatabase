package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;
import net.ximatai.muyun.database.core.annotation.Table;
import net.ximatai.muyun.database.core.builder.PredefinedColumn;

import java.sql.Timestamp;
import java.util.Date;

@Table(name = "test_entity")
public class TestEntityForMysql {

    @Id(value = PredefinedColumn.Id.MYSQL)
    @Column
    public String id;

    @Column(length = 20, comment = "名称", defaultValue = "test")
    public String name;

    @Column(comment = "年龄")
    public int age;

    @Column(precision = 10, scale = 2)
    public double price;

    @Column
    public boolean flag;

    @Column(defaultValue = "CURRENT_TIMESTAMP")
    public Date date;

    public Timestamp created;

}
