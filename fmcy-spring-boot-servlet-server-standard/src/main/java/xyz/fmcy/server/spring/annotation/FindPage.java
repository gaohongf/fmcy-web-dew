package xyz.fmcy.server.spring.annotation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.spring.core.Annotations;
import xyz.fmcy.server.spring.core.ParamAnnotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FindPage {
    Class<? extends QueryConfiguration> query();

    RequestMapping mapping() default @RequestMapping(
            method = RequestMethod.POST, value = "/find/page"
    );

    Class<?> viewType() default Object.class;

    Annotations[] otherMethodAnnotations() default {};

    ParamAnnotations[] otherParamAnnotations() default {};
}
