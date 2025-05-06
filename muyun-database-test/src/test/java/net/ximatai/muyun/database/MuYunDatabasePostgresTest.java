package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.builder.Column;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static net.ximatai.muyun.database.core.builder.Column.ID_POSTGRES;

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
        return ID_POSTGRES;
    }

    @Override
    JdbcDatabaseContainer getContainer() {
        return container;
    }
}
