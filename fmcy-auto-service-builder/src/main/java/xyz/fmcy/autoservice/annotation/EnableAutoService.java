package xyz.fmcy.autoservice.annotation;

import org.springframework.context.annotation.Import;
import xyz.fmcy.autoservice.core.ServiceDriver;
import xyz.fmcy.autoservice.core.XMapperHelper;
import xyz.fmcy.autoservice.core.XServiceHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({XServiceHelper.class, XMapperHelper.class, ServiceDriver.class})
public @interface EnableAutoService {
}
