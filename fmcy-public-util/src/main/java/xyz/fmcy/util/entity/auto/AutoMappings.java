package xyz.fmcy.util.entity.auto;

import java.lang.annotation.*;

/**
 * @author 付高宏
 * @date 2023/1/31 14:55
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoMappings {
    AutoMapping[] value();
}
