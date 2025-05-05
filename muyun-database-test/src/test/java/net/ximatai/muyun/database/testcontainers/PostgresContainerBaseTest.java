package net.ximatai.muyun.database.testcontainers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class PostgresContainerBaseTest {

    private DataSource dataSource;

    @Container
    private static final JdbcDatabaseContainer postgresContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Test
    void testDatabaseStarted() {
        assertTrue(postgresContainer.isRunning());
    }

    @Test
    void testDataSourceAlready() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            assertFalse(connection.isClosed());
        }
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(postgresContainer.getJdbcUrl());
            config.setUsername(postgresContainer.getUsername());
            config.setPassword(postgresContainer.getPassword());
            config.setDriverClassName(postgresContainer.getDriverClassName());
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

}
