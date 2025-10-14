package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;
import net.ximatai.muyun.database.core.annotation.Sequence;
import net.ximatai.muyun.database.core.annotation.Table;
import net.ximatai.muyun.database.core.builder.ColumnType;

import java.util.Date;

@Table(name = "test_entity")
public class TestEntityForMysql extends TestEntityBase {

    @Id
    @Sequence
    @Column(type = ColumnType.BIGINT, defaultValue = "AUTO_INCREMENT")
    public String id;

    @Column(defaultValue = "CURRENT_TIMESTAMP")
    public Date date;

}
