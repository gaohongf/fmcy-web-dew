package xyz.fmcy.util.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 付高宏
 * @date 2023/2/9 10:37
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Entity {

    Type[] type();

    enum Type {
        PO,
        DO,
        DTO,
        VO,
        REQUEST,
        RESPONSE,
        ANY
    }
}
