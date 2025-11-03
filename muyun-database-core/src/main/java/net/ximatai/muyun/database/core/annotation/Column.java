package net.ximatai.muyun.database.core.annotation;

import net.ximatai.muyun.database.core.builder.ColumnType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";

    ColumnType type() default ColumnType.UNKNOWN;

    int length() default 0;

    int precision() default 0;

    int scale() default 0;

    boolean nullable() default true;

    boolean unique() default false;

    String comment() default "";

    String defaultValue() default "";

    Default defaultVal() default @Default(unset = true);
}

