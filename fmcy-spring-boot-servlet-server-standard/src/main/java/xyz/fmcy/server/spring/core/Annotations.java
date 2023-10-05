package xyz.fmcy.server.spring.core;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Annotations {
    Class<? extends Annotation> type();

    Field[] fields() default {};

    @interface Field {
        String name();

        @Language(value = "json")
        String value();
    }
}
