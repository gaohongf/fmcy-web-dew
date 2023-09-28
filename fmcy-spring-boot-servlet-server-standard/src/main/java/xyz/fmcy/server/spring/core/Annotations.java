package xyz.fmcy.server.spring.core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Annotations {
    Class<? extends Annotation> type();

    Field[] fields() default {};

    @interface Field {
        String name();

        String value();
    }
}
