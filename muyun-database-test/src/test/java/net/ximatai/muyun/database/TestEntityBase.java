package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.*;

import java.util.Date;

@Table(name = "test_entity")
@CompositeIndex(columns = {"name", "age"}, unique = true)
@CompositeIndex(columns = {"name", "flag"})
public class TestEntityBase {

    @Default.Varchar("test_name")
    @Column(length = 20, comment = "名称")
    public String name;

    @Indexed(unique = true)
    @Column(comment = "学号")
    public int code;

    @Default.Number(12)
    @Column(comment = "年龄")
    public int age;

    @Default.Decimal(1.23)
    @Column(precision = 10, scale = 2)
    public double price;

    @Default.Bool(true)
    @Indexed
    @Column
    public boolean flag;

    @Default.Express("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    public Date creatTime;

}
