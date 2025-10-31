package net.ximatai.muyun.database.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CompositeIndexes.class)
public @interface CompositeIndex {

    String[] columns();

    String name() default "";

    boolean unique() default false;
}
