package xyz.fmcy.server.spring;

import org.springframework.context.annotation.Import;
import xyz.fmcy.server.spring.core.ServerConfiguration;
import xyz.fmcy.server.spring.core.ServerResultCodeConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ServerConfiguration.class, ServerResultCodeConfiguration.class})
public @interface EnableSimpleServerConfiguration {
}
