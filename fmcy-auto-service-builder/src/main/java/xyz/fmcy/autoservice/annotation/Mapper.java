package xyz.fmcy.autoservice.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Mapper {
    String value() default "";

    boolean enable() default true;
}
