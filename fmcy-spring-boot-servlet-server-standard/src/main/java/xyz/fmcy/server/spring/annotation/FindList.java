package xyz.fmcy.server.spring.annotation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.spring.abandon.QueryProxy;
import xyz.fmcy.server.spring.core.Annotations;
import xyz.fmcy.server.spring.core.ParamAnnotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FindList {
    Class<? extends QueryConfiguration> query() default QueryProxy.class;

    RequestMapping mapping() default @RequestMapping(
            method = RequestMethod.POST, value = "/find/list"
    );

    Class<?> viewType() default Object.class;

    Annotations[] otherMethodAnnotations() default {};

    ParamAnnotations[] otherParamAnnotations() default {};
}
