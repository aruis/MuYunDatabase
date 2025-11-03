package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Default;
import net.ximatai.muyun.database.core.annotation.Id;
import net.ximatai.muyun.database.core.annotation.Sequence;
import net.ximatai.muyun.database.core.builder.ColumnType;

public class TestEntityForMysql extends TestEntityBase {

    @Id
    @Sequence
    @Column(type = ColumnType.BIGINT, defaultVal = @Default(express = "AUTO_INCREMENT"))
    public String id;

}
