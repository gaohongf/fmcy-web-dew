package xyz.fmcy.validation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 付高宏
 * @date 2023/2/7 17:19
 */
@Import({ValidationExceptionEntry.class,SpELValidator.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableArgValidation {
}
