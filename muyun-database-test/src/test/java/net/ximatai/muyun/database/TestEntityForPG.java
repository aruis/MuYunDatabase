package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;
import net.ximatai.muyun.database.core.annotation.Table;

import java.util.Date;

@Table(name = "test_entity")
public class TestEntityForPG extends TestEntityBase {

    @Id
    @Column(defaultValue = "gen_random_uuid()")
    public String id;

    @Column
    public Date date;

}
