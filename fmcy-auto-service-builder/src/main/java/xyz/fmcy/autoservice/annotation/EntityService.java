package xyz.fmcy.autoservice.annotation;

import xyz.fmcy.entity.annotation.EM;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EntityService {
    EM value();

    Service service() default @Service;

    Mapper mapper() default @Mapper;
}
