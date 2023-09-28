package xyz.fmcy.entity.annotation;

import org.springframework.context.annotation.Import;
import xyz.fmcy.entity.E;
import xyz.fmcy.entity.SpringEntityMapperPool;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({SpringEntityMapperPool.class, E.class})
public @interface EnableEntityMapperPool {
    /**
     * //无效属性，先放着
     */
    String[] scans() default {""};
}
