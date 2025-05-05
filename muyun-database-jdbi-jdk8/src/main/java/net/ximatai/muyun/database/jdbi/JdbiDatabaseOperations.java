package net.ximatai.muyun.database.jdbi;

import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.IMetaDataLoader;
import net.ximatai.muyun.database.core.metadata.DBColumn;
import net.ximatai.muyun.database.core.metadata.DBTable;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JdbiDatabaseOperations implements IDatabaseOperations {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Jdbi jdbi;
    private JdbiMetaDataLoader metaDataLoader;
    private RowMapper rowMapper = new MapMapper();

    public RowMapper getRowMapper() {
        return rowMapper;
    }

    public JdbiDatabaseOperations setRowMapper(RowMapper rowMapper) {
        Objects.requireNonNull(rowMapper);
        this.rowMapper = rowMapper;
        return this;
    }

    public JdbiDatabaseOperations(Jdbi jdbi, JdbiMetaDataLoader metaDataLoader) {
        this.jdbi = jdbi;
        this.metaDataLoader = metaDataLoader;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public IMetaDataLoader getMetaDataLoader() {
        return metaDataLoader;
    }

    @Override
    public Map<String, ?> transformDataForDB(DBTable dbTable, Map<String, ?> data) {
        // 创建一个新的 Map 来存储修改后的数据
        Map<String, Object> transformedData = new HashMap<>(data);

        // 遍历原数据并修改
        transformedData.forEach((k, v) -> {
            DBColumn dbColumn = dbTable.getColumn(k);
            if (dbColumn != null) {
                transformedData.put(k, getDBValue(v, dbColumn.getType()));
            }
        });

        return transformedData;
    }

    public Object getDBValue(Object value, String type) {
        if (value == null) {
            return null;
        }

//        if (value instanceof List) {
//            return ((List<?>) value).toArray();
//        }

        // 以_开头的是数组类型，形如：_int4，目前只考虑支持 varchar、integer 和 bool
        if (type.startsWith("_")) {
            if (value instanceof java.sql.Array) {
                return value; // 已经是数组类型，直接返回
            }

            Object[] arrayValue;
            if (value instanceof String) {
                String string = (String) value;
                arrayValue = string.split(",");
            } else if (value instanceof List<?>) {
                List<?> list = (List<?>) value;
                arrayValue = list.toArray();
            } else {
                return value; // 不支持的输入类型，直接返回
            }

            String subType = type.substring(1);

            switch (subType) {
                case "varchar":
                    return Arrays.stream(arrayValue)
                            .map(Object::toString)
                            .toArray(String[]::new);
                case "int4":
                    return Arrays.stream(arrayValue)
                            .map(val -> Integer.parseInt(val.toString()))
                            .toArray(Integer[]::new);
                case "bool":
                    return Arrays.stream(arrayValue)
                            .map(val -> Boolean.parseBoolean(val.toString()))
                            .toArray(Boolean[]::new);
                default:
                    return value; // 不支持的类型，返回原值
            }

        }

        switch (type) {
            case "varchar":
                return value.toString();
            case "int8":
                return convertToBigInteger(value);
            case "int4":
            case "int2":
                return convertToInteger(value);
            case "bool":
                return isTrue(value);
            case "date":
            case "timestamp":
                return handleDateTimestamp(value);
            case "numeric":
                return convertToBigDecimal(value);
//            case "json", "jsonb" -> convertToJson(value);
            case "bytea":
                return convertToByteArray(value);
            default:
                return value;
        }

    }

    @Override
    public <T> T insert(String sql, Map<String, ?> params, String pk, Class<T> idType) {
        return getJdbi().withHandle(handle -> handle.createUpdate(sql)
                .bindMap(params)
                .executeAndReturnGeneratedKeys(pk).mapTo(idType).one());
    }

    @Override
    public <T> List<T> batchInsert(String sql, List<? extends Map<String, ?>> paramsList, String pk, Class<T> idType) {
        return getJdbi().withHandle(handle -> {
            List<T> generatedKeys = new ArrayList<>();
            PreparedBatch batch = handle.prepareBatch(sql);

            for (Map<String, ?> params : paramsList) {
                batch.bindMap(params).add();
            }

            batch.executePreparedBatch(pk)
                    .mapTo(idType)
                    .forEach(generatedKeys::add);

            return generatedKeys;
        });
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> (Map<String, Object>) handle.createQuery(sql)
                .bindMap(params)
                .map(getRowMapper())
                .findOne()
                .orElse(null));

    }

    @Override
    public Map<String, Object> row(String sql, List<?> params) {
        Object row = getJdbi().withHandle(handle -> {
            Query query = handle.createQuery(sql);
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.map(getRowMapper()).findOne().orElse(null);
        });

        return (Map<String, Object>) row;
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> handle.createQuery(sql)
                .bindMap(params)
                .map(getRowMapper())
                .list());
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<?> params) {
        return getJdbi().withHandle(handle -> {
            Query query = handle.createQuery(sql);

            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.map(getRowMapper()).list();
        });
    }

    @Override
    public Integer update(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> handle.createUpdate(sql)
                .bindMap(params)
                .execute());
    }

    @Override
    public Integer update(String sql, List<?> params) {
        return getJdbi().withHandle(handle -> {
            Update query = handle.createUpdate(sql);

            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.execute();
        });
    }

    @Override
    public Integer execute(String sql) {
        return getJdbi().withHandle(handle -> handle.execute(sql));
    }

    @Override
    public Integer execute(String sql, Object... params) {
        return getJdbi().withHandle(handle -> handle.execute(sql, params));
    }

    @Override
    public Integer execute(String sql, List<?> params) {
        return getJdbi().withHandle(handle -> handle.execute(sql, params.toArray()));
    }

    public Array createArray(List list, String type) {
        try {
            return getJdbi().withHandle(handle -> {
                Connection connection = handle.getConnection();
                return connection.createArrayOf(type, list.toArray());
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private BigInteger convertToBigInteger(Object value) {
        if (value instanceof String) {
            return new BigInteger((String) value);
        } else if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        throw new IllegalArgumentException("Cannot convert to BigInteger: " + value);
    }

    private Integer convertToInteger(Object value) {
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Cannot convert to Integer: " + value);
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof String && !isBlank((String) value)) {
            return new BigDecimal((String) value);
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return null;
    }

    private byte[] convertToByteArray(Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return value.toString().getBytes();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isTrue(Object value) {
        return Objects.equals(value, Boolean.TRUE) || "true".equalsIgnoreCase(value.toString());
    }

    public static Date stringToSqlDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }

        try {
            LocalDate localDate = LocalDate.parse(dateString.substring(0, 10), DATE_FORMATTER);
            return Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
    }

    public static Timestamp stringToSqlTimestamp(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            if (dateString.length() == 10) {
                dateString += " 00:00:00";
            }
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
            return Timestamp.valueOf(localDateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format: " + dateString);
        }
    }

    public static Timestamp handleDateTimestamp(Object value) {
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        } else if ("".equals(value)) {
            return null;
        } else if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        } else if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        } else if (value instanceof String) {
            return stringToSqlTimestamp((String) value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }
}
