package xyz.fmcy.server.spring.annotation;

import java.io.Serializable;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AddGenerator {
    Class<? extends Serializable> insertType() default Serializable.class;

    EnableAddOne enableOne() default @EnableAddOne;

    EnableAddList enableList() default @EnableAddList;

    @interface EnableAddOne {
        boolean value() default true;

        AddOne annotation() default @AddOne;
    }

    @interface EnableAddList {
        boolean value() default true;

        AddList annotation() default @AddList;
    }
}
