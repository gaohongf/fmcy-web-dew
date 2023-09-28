package xyz.fmcy.entity.annotation;

import xyz.fmcy.util.entity.GroupEntityMapper;
import xyz.fmcy.util.entity.auto.DefEntityMapperHandler;
import xyz.fmcy.util.entity.auto.EntityMapperHandler;
import xyz.fmcy.util.entity.auto.MapperGroup;

import java.lang.annotation.*;

/**
 * EM is Entity Mapping,标识一个类被赋予与另一个类的映射关系
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EM {
    /**
     * 目标类
     */
    Class<?> target();

    /**
     * 反向转换配置
     */
    Mapping reverse() default @Mapping;

    /**
     * 正向转换配置
     */
    Mapping becomeOf() default @Mapping;

    @interface Mapping {
        boolean enable() default true;

        Class<? extends EntityMapperHandler> handler() default DefEntityMapperHandler.class;

        String[] demands() default {};

        MapperGroup group() default @MapperGroup(groupKey = GroupEntityMapper.DEF_GROUP);
    }
}
