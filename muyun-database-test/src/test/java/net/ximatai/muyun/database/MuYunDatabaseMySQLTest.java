package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableBuilder;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBTable;
import net.ximatai.muyun.database.jdbi.JdbiDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiMetaDataLoader;
import net.ximatai.muyun.database.testcontainers.MySQLContainerBaseTest;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static net.ximatai.muyun.database.core.builder.Column.ID_MYSQL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MuYunDatabaseMySQLTest extends MySQLContainerBaseTest {

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

        Assertions.assertEquals("mysql", info.getTypeName().toLowerCase());
        assertNotNull(info.getDefaultSchema());

    }

    @Test
    void testTableBuilder() {
        TableWrapper basic = TableWrapper.withName("basic")
                .setPrimaryKey(ID_MYSQL)
                .addColumn(Column.of("v_name").setLength(20).setIndexed())
                .addColumn("i_age");

        boolean build = new TableBuilder(db).build(basic);

        assertTrue(build);

        DBInfo info = loader.getDBInfo();

        DBTable table = info.getDefaultSchema().getTable("basic");

        assertNotNull(table);

        assertTrue(table.contains("id"));
        assertTrue(table.contains("v_name"));
        assertTrue(table.contains("i_age"));

        assertTrue(table.getColumn("id").isPrimaryKey());
    }

}
