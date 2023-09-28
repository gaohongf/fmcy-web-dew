package xyz.fmcy.mybatisplus.util.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Service
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EMXService {

    Class<?> source();

    Class<?> target();

    @AliasFor(
            annotation = Component.class
    )
    String value() default "";

}
