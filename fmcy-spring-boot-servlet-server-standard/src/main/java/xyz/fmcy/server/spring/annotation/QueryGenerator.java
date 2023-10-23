package xyz.fmcy.server.spring.annotation;

import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.spring.abandon.QueryProxy;

import java.io.Serializable;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryGenerator {
    Class<? extends QueryConfiguration> query() default QueryProxy.class;

    Class<?> viewType() default Object.class;

    EnableFindById enableFindById() default @EnableFindById;

    EnableList enableList() default @EnableList;

    EnablePage enablePage() default @EnablePage;

    @interface EnableFindById {

        boolean value() default true;

        FindById annotation() default @FindById;
    }

    @interface EnablePage {

        boolean value() default true;

        FindPage annotation() default @FindPage;
    }

    @interface EnableList {

        boolean value() default true;

        FindList annotation() default @FindList;
    }
}



