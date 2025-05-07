package net.ximatai.muyun.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableBuilder;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.database.core.metadata.DBColumn;
import net.ximatai.muyun.database.core.metadata.DBInfo;
import net.ximatai.muyun.database.core.metadata.DBTable;
import net.ximatai.muyun.database.jdbi.JdbiDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiMetaDataLoader;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MuYunDatabaseBaseTest {

    private DataSource dataSource;

    Jdbi jdbi;
    JdbiMetaDataLoader loader;
    JdbiDatabaseOperations db;

    abstract DatabaseType getDatabaseType();

    abstract Column getPrimaryKey();

    abstract JdbcDatabaseContainer getContainer();

    private boolean tableCreated = false;

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

    @BeforeEach
    void beforeEach() {
        if (!tableCreated) { // 测试之前要先创建表
            testTableBuilder();
        }
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
                .addColumn(Column.of("n_price").setPrecision(10).setScale(2))
                .addColumn("b_flag")
                .addColumn("d_date")
                .addColumn(Column.of("t_create").setDefaultValue("CURRENT_TIMESTAMP"));

        new TableBuilder(db).build(basic);

        DBInfo info = loader.getDBInfo();

        DBTable table = info.getDefaultSchema().getTable("basic");

        assertNotNull(table);

        assertTrue(table.contains("id"));
        assertTrue(table.contains("v_name"));
        assertTrue(table.contains("b_flag"));
        assertTrue(table.contains("i_age"));

        assertTrue(table.getColumn("id").isPrimaryKey());

        tableCreated = true;
    }

    @Test
    void testTableBuilderChangeLength() {
        String schema = loader.getDBInfo().getDefaultSchemaName();

        TableWrapper basic = TableWrapper.withName("basic")
                .setPrimaryKey(getPrimaryKey())
                .setComment("测试表")
                .addColumn(Column.of("v_name").setLength(20).setIndexed().setComment("名称").setDefaultValue("test"))
                .addColumn(Column.of("i_age").setComment("年龄"))
                .addColumn(Column.of("n_price").setPrecision(10).setScale(2))
                .addColumn("b_flag")
                .addColumn("d_date")
                .addColumn(Column.of("t_create").setDefaultValue("CURRENT_TIMESTAMP"));

        new TableBuilder(db).build(basic);

        Map body = Map.of("v_name", "abcd_efgh",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );

        String id = db.insertItem(schema, "basic", body);

        TableWrapper basic2 = TableWrapper.withName("basic")
                .addColumn(Column.of("v_name").setLength(12).setIndexed().setComment("名称").setDefaultValue("test"));

        new TableBuilder(db).build(basic2);

        DBInfo info = loader.getDBInfo();

        DBTable table = info.getDefaultSchema().getTable("basic");
        DBColumn vName = table.getColumn("v_name");

        assertEquals(12, vName.getLength());
    }

    @Test
    void testSimpleInsert() {
        String schema = loader.getDBInfo().getDefaultSchemaName();
        Map body = Map.of("v_name", "test_name",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );

        String id = db.insertItem(schema, "basic", body);
        assertNotNull(id);

        Map<String, Object> item = db.getItem(schema, "basic", id);

        assertNotNull(item);
        assertEquals("test_name", item.get("v_name"));
        assertEquals(5, item.get("i_age"));
        assertEquals(true, item.get("b_flag"));
        assertEquals(0, BigDecimal.valueOf(10.2).compareTo((BigDecimal) item.get("n_price")));
        assertEquals(LocalDate.of(2024, 1, 1), ((Date) item.get("d_date")).toLocalDate());
    }

    @Test
    void testBatchInsert() {
        String schema = loader.getDBInfo().getDefaultSchemaName();
        Map<String, ?> body = Map.of("v_name", "test_name1",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );
        Map<String, ?> body2 = Map.of("v_name", "test_name2",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );

        List<String> ids = db.insertList(schema, "basic", List.of(body, body2));

        assertNotNull(ids);
        assertEquals(2, ids.size());
    }

    @Test
    void testUpdate() {
        String schema = loader.getDBInfo().getDefaultSchemaName();
        Map body = Map.of("v_name", "test_name",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );

        String id = db.insertItem(schema, "basic", body);
        assertNotNull(id);

        Map<String, Object> item = db.getItem(schema, "basic", id);

        assertEquals("test_name", item.get("v_name"));

        db.updateItem(schema, "basic", Map.of(
                "id", id,
                "v_name", "test_name2"));

        item = db.getItem(schema, "basic", id);

        assertEquals("test_name2", item.get("v_name"));
    }

    @Test
    void testDelete() {
        String schema = loader.getDBInfo().getDefaultSchemaName();
        Map body = Map.of("v_name", "test_name",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );

        String id = db.insertItem(schema, "basic", body);
        assertNotNull(id);

        Integer deleteSize = db.deleteItem(schema, "basic", id);
        assertEquals(1, deleteSize);

        Map<String, Object> item = db.getItem(schema, "basic", id);

        assertNull(item);
    }

    @Test
    void testQuery() {
        String schema = loader.getDBInfo().getDefaultSchemaName();
        Map body = Map.of("v_name", "test_name_x",
                "i_age", 5,
                "b_flag", true,
                "n_price", 10.2,
                "d_date", "2024-01-01"
        );

        String id = db.insertItem(schema, "basic", body);

        List<Map<String, Object>> queried = db.query("select * from basic where id = ?", List.of(id));

        assertEquals(1, queried.size());

        List<Map<String, Object>> queried2 = db.query("select * from basic where id = ?", id);

        assertEquals(1, queried2.size());

        List<Map<String, Object>> queried3 = db.query("select * from basic where id = :id", Map.of("id", id));

        assertEquals(1, queried3.size());

        Map<String, Object> row = db.row("select * from basic where id = :id", Map.of("id", id));

        assertEquals("test_name_x", queried.getFirst().get("v_name"));
        assertEquals("test_name_x", queried2.getFirst().get("v_name"));
        assertEquals("test_name_x", queried3.getFirst().get("v_name"));
        assertEquals("test_name_x", row.get("v_name"));

    }

}
