package xyz.fmcy.autoservice.annotation;

import xyz.fmcy.server.database.XService;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Service {
    String value() default "";

    boolean enable() default true;

    Class<? extends XService> extend() default XService.class;
}
