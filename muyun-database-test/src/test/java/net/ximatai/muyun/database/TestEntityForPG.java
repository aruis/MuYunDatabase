package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;
import net.ximatai.muyun.database.core.annotation.Table;
import net.ximatai.muyun.database.core.builder.PredefinedColumn;

import java.util.Date;

@Table(name = "test_entity")
public class TestEntityForPG extends TestEntityBase {

    @Id(value = PredefinedColumn.Id.POSTGRES)
    @Column
    public String id;

    @Column
    public Date date;

}
