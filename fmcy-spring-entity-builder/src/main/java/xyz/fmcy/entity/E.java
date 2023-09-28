package xyz.fmcy.entity;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import xyz.fmcy.util.entity.DemandHandler;
import xyz.fmcy.util.entity.EntityMapper;
import xyz.fmcy.util.entity.GroupEntityMapper;
import xyz.fmcy.util.entity.auto.DefEntityMapperHandler;
import xyz.fmcy.util.entity.auto.EntityMapperPool;

@Configuration
public class E {
    private static EntityMapperPool pool;
    @Lazy
    E(@Qualifier("entityMapperPool") EntityMapperPool pool) {
        E.pool = pool;
    }

    @Bean
    public DefEntityMapperHandler<?, ?> defEntityMapperHandler() {
        return new DefEntityMapperHandler<>();
    }

    public static <O, T> DemandHandler<O, T> getMapper(Class<O> sourceClazz, Class<T> targetClazz) {
        return getMapper(sourceClazz, targetClazz, GroupEntityMapper.DEF_GROUP);
    }

    public static <O, T> DemandHandler<O, T> getMapper(Class<O> sourceClazz, Class<T> targetClazz, Object group) {
        return pool.mapper(sourceClazz, targetClazz, group);
    }

    public static <O, T> DemandHandler<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz) {
        return getMapper(sourceClazz, targetClazz);
    }

    public static <O, T> DemandHandler<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz, Object group) {
        return getMapper(sourceClazz, targetClazz, group);
    }

    public static <O, T> void addMapper(String groupKey, EntityMapper<O, T> entityMapper) {
        pool.addMapper(groupKey, entityMapper);
    }
}
