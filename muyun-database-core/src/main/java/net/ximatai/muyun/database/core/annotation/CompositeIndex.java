package net.ximatai.muyun.database.core.annotation;

import java.lang.annotation.*;

/**
 * 复合索引注解
 * 用于定义多列组合索引，支持唯一索引
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CompositeIndexes.class)
public @interface CompositeIndex {

    /**
     * 索引包含的列名数组
     * 列顺序影响索引效率，建议按查询频率排序
     */
    String[] columns();

    /**
     * 索引名称，为空时自动生成
     */
    String name() default "";

    /**
     * 是否唯一索引
     * true表示唯一约束，false为普通索引
     */
    boolean unique() default false;
}
