package net.ximatai.muyun.database;

import net.ximatai.muyun.database.core.builder.Column;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static net.ximatai.muyun.database.core.builder.Column.ID_MYSQL;

@Testcontainers
public class MuYunDatabaseMySQLTest extends MuYunDatabaseBaseTest {

    @Container
//    private static final JdbcDatabaseContainer postgresContainer = new MySQLContainer("mysql:8.4.5")
    private static final JdbcDatabaseContainer container = new MySQLContainer()
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Override
    DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }

    @Override
    Column getPrimaryKey() {
        return ID_MYSQL;
    }

    @Override
    JdbcDatabaseContainer getContainer() {
        return container;
    }
}
