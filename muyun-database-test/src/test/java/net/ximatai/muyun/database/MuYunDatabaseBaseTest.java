package net.ximatai.muyun.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableBuilder;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBTable;
import net.ximatai.muyun.database.jdbi.JdbiDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiMetaDataLoader;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MuYunDatabaseBaseTest {

    private DataSource dataSource;

    Jdbi jdbi;
    JdbiMetaDataLoader loader;
    JdbiDatabaseOperations db;

    abstract DatabaseType getDatabaseType();

    abstract Column getPrimaryKey();

    abstract JdbcDatabaseContainer getContainer();

    DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getContainer().getJdbcUrl());
            config.setUsername(getContainer().getUsername());
            config.setPassword(getContainer().getPassword());
            config.setDriverClassName(getContainer().getDriverClassName());
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

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

        Assertions.assertEquals(getDatabaseType().name().toLowerCase(), info.getTypeName().toLowerCase());
        assertNotNull(info.getDefaultSchema());
    }

    @Test
    void testTableBuilder() {
        TableWrapper basic = TableWrapper.withName("basic")
                .setPrimaryKey(getPrimaryKey())
                .setComment("测试表")
                .addColumn(Column.of("v_name").setLength(20).setIndexed().setComment("名称").setDefaultValue("test"))
                .addColumn(Column.of("i_age").setComment("年龄"))
                .addColumn("n_price")
                .addColumn("b_flag")
                .addColumn("d_date")
                .addColumn(Column.of("t_create").setDefaultValue("CURRENT_TIMESTAMP"));

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
