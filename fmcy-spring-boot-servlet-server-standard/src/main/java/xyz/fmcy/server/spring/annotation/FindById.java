package xyz.fmcy.server.spring.annotation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xyz.fmcy.server.spring.core.Annotations;
import xyz.fmcy.server.spring.core.ParamAnnotations;

import java.io.Serializable;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FindById {
    Class<? extends Serializable> idClass() default Integer.class;

    RequestMapping mapping() default @RequestMapping(path = "/find/one/{id}", method = RequestMethod.GET);

    Class<?> viewType() default Object.class;

    PathVariable pathVariable() default @PathVariable("id");

    Annotations[] otherMethodAnnotations() default {};

    ParamAnnotations[] otherParamAnnotations() default {};
}
