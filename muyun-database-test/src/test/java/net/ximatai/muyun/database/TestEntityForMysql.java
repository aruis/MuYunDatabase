package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;
import net.ximatai.muyun.database.core.annotation.Table;
import net.ximatai.muyun.database.core.builder.PredefinedColumn;

import java.util.Date;

@Table(name = "test_entity")
public class TestEntityForMysql extends TestEntityBase {

    @Id(value = PredefinedColumn.Id.MYSQL)
    @Column
    public String id;

    @Column(defaultValue = "CURRENT_TIMESTAMP")
    public Date date;

}
