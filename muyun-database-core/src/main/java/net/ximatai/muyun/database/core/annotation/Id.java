package net.ximatai.muyun.database.core.annotation;

import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.PredefinedColumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    String name() default "";

    PredefinedColumn.Id value();

}
