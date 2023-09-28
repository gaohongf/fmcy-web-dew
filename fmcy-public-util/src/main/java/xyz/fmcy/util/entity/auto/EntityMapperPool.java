package xyz.fmcy.util.entity.auto;

import xyz.fmcy.util.entity.EntityMapper;
import xyz.fmcy.util.entity.DemandHandler;

/**
 * @author 付高宏
 * @date 2023/1/31 14:14
 */
public interface EntityMapperPool {
    <O, T> DemandHandler<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz);

    <O, T> void addMapper(String groupKey, EntityMapper<O, T> entityMapper);

    <O, T> DemandHandler<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz, Object groupKey);
}
