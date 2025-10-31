package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;

import java.util.Date;

public class TestEntityForPG extends TestEntityBase {

    @Id
    @Column(defaultValue = "gen_random_uuid()")
    public String id;

    @Column
    public Date date;

}
