package xyz.fmcy.util.entity.auto;

import xyz.fmcy.util.entity.EntityMapper;
import xyz.fmcy.util.entity.GroupEntityMapper;

import java.lang.annotation.*;

/**
 * @author 付高宏
 * @date 2023/1/31 11:51
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoMapping {
    Class<?> target();

    Class<? extends EntityMapperHandler> handler() default DefEntityMapperHandler.class;

    MapperGroup group() default @MapperGroup(groupKey = GroupEntityMapper.DEF_GROUP);

    String[] simpleDemand() default {};

    boolean reverse() default false;
}
