package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Id;

public class TestEntityForPG extends TestEntityBase {

    @Id
    @Column(defaultValue = "gen_random_uuid()")
    public String id;

}
