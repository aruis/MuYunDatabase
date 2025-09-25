package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.PredefinedColumn;
import net.ximatai.muyun.database.core.builder.TableBuilder;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBTable;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class MuYunDatabasePostgresTest extends MuYunDatabaseBaseTest {

    @Container
//    private static final JdbcDatabaseContainer postgresContainer = new MySQLContainer("mysql:8.4.5")
    private static final JdbcDatabaseContainer container = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Override
    DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRESQL;
    }

    @Override
    Column getPrimaryKey() {
        return PredefinedColumn.Id.POSTGRES.toColumn();
    }

    @Override
    JdbcDatabaseContainer getContainer() {
        return container;
    }

    @Test
    void testTableBuilderWithEntity() {

        new TableBuilder(db).build(TestEntityForPG.class);

        DBInfo info = loader.getDBInfo();

        DBTable table = info.getDefaultSchema().getTable("test_entity");

        assertNotNull(table);

        assertTrue(table.contains("id"));
        assertTrue(table.contains("name"));
        assertTrue(table.contains("age"));
        assertTrue(table.contains("price"));

        assertTrue(table.getColumn("id").isPrimaryKey());
    }
}
