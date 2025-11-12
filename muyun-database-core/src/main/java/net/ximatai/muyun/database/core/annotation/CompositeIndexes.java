package net.ximatai.muyun.database.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 复合索引容器注解
 * 用于在同一实体类上定义多个复合索引
 * 作为@CompositeIndex的容器注解，支持重复注解功能
 *
 * 使用示例：
 * {@code
 * @CompositeIndexes({
 *     @CompositeIndex(columns = {"name", "age"}, name = "idx_name_age"),
 *     @CompositeIndex(columns = {"email", "phone"}, unique = true)
 * })
 * public class User {
 *     // 实体类字段
 * }
 * }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CompositeIndexes {
    /**
     * 复合索引注解数组
     * 用于定义多个复合索引配置
     *
     * @return CompositeIndex注解数组
     */
    CompositeIndex[] value();
}
