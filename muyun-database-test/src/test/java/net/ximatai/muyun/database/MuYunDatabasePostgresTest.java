package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.builder.TableBuilder;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBTable;
import net.ximatai.muyun.database.jdbi.JdbiDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiMetaDataLoader;
import net.ximatai.muyun.database.testcontainers.PostgresContainerBaseTest;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static net.ximatai.muyun.database.core.builder.Column.ID_POSTGRES;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MuYunDatabasePostgresTest extends PostgresContainerBaseTest {

    Jdbi jdbi;
    JdbiMetaDataLoader loader;
    JdbiDatabaseOperations db;

    @BeforeAll
    void setUp() {
        jdbi = Jdbi.create(getDataSource())
                .setSqlLogger(new Slf4JSqlLogger());
        loader = new JdbiMetaDataLoader(jdbi);
        db = new JdbiDatabaseOperations(jdbi, loader);
    }

    @Test
    void testGetDBInfo() {

        DBInfo info = loader.getDBInfo();

        Assertions.assertEquals("postgresql", info.getTypeName().toLowerCase());
        Assertions.assertNotNull(info.getSchema("public"));

    }

    @Test
    void testTableBuilder() {
        TableWrapper basic = TableWrapper.withName("basic")
                .setPrimaryKey(ID_POSTGRES)
                .addColumn("v_name")
                .addColumn("i_age");

        boolean build = new TableBuilder(db).build(basic);

        Assertions.assertTrue(build);

        DBInfo info = loader.getDBInfo();

        DBTable table = info.getDefaultSchema().getTable("basic");

        Assertions.assertNotNull(table);
    }

}
