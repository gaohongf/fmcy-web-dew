package xyz.fmcy.server.spring.annotation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import xyz.fmcy.server.spring.core.Annotations;
import xyz.fmcy.server.spring.core.ParamAnnotations;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AddList {
    Class<? extends Serializable> insertType() default Serializable.class;

    RequestMapping mapping() default @RequestMapping(path = "/add/rows", method = RequestMethod.POST);

    Annotations[] otherMethodAnnotations() default {};

    ParamAnnotations[] otherParamAnnotations() default {};
}
