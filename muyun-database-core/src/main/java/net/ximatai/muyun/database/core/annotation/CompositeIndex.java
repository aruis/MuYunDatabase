package net.ximatai.muyun.database.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CompositeIndexes.class)
public @interface CompositeIndex {
    /**
     * 组成联合索引的字段名数组
     */
    String[] columns();

    /**
     * 索引名称（可选）
     */
    String name() default "";

    /**
     * 是否唯一索引
     */
    boolean unique() default false;
}
