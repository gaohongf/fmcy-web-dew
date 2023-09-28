package xyz.fmcy.util.entity.auto;

import xyz.fmcy.util.auto.PackageScanner;
import xyz.fmcy.util.entity.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author 付高宏
 * @date 2023/1/31 14:11
 */
public class EntityMapperTools {

    public final static EntityMapperPool pool;
    private static final String POOL_CLASS = "pool-class";
    private static final String SCAN_ROOT = "scan-root";

    private static final class DefaultEntityMapperPool implements EntityMapperPool {
        private final Map<MapperKey, EntityMapper<?, ?>> entityMappers;
        private final List<String> packages;

        private DefaultEntityMapperPool() {
            this("");
        }

        @SuppressWarnings("unchecked")
        private DefaultEntityMapperPool(String scanPack) {
            packages = new ArrayList<>();
            entityMappers = new HashMap<>();
            PackageScanner.build(clazz -> packages.addAll(Arrays.stream(clazz.getAnnotation(AutoEntityMappingScans.class).scans()).toList()))
                    .filter(PackageScanner.hasAnnotation(AutoEntityMappingScans.class))
                    .scanPacket(scanPack);
            if (packages.size() == 0) packages.add("");
            PackageScanner.build(clazz -> {
                        AutoMappings autoMappings = clazz.getAnnotation(AutoMappings.class);
                        AutoMapping autoMapping = clazz.getAnnotation(AutoMapping.class);
                        List<AutoMapping> list;
                        if (autoMappings != null) {
                            list = new ArrayList<>(List.of(autoMappings.value()));
                        } else {
                            list = new ArrayList<>();
                        }
                        if (autoMapping != null) {
                            list.add(autoMapping);
                        }
                        list.forEach(mapping -> {
                            Class<?> target = mapping.target();
                            MapperKey mapperKey = mapping.reverse() ? new MapperKey(target, clazz) : new MapperKey(clazz, target);
                            EntityMapper<Object, Object> build = (EntityMapper<Object, Object>) (mapping.reverse() ? buildEntityMapper(target, clazz, mapping) : buildEntityMapper(clazz, target, mapping));
                            EntityMapper<Object, Object> entityMapper = (EntityMapper<Object, Object>) entityMappers.get(mapperKey);
                            if (entityMapper != null) {
                                MapperGroup group = mapping.group();
                                entityMapper.want(build.group()
                                        .obtain(group.groupKey())
                                        .group(group.groupKey()));
                            } else {
                                entityMappers.put(mapperKey, build);
                            }
                        });
                    })
                    .filter((c) -> PackageScanner.hasAnnotation(AutoMapping.class).test(c)
                            || PackageScanner.hasAnnotation(AutoMappings.class).test(c)
                    )
                    .scanPacket(packages.toArray(String[]::new));
        }

        @SuppressWarnings("unchecked")
        private EntityMapper<?, ?> buildEntityMapper(Class<?> sourceClass, Class<?> targetClass, AutoMapping autoMapping) {
            return buildEntityMapper(sourceClass, targetClass, (Class<EntityMapperHandler<?, ?>>) autoMapping.handler(), autoMapping.group().groupKey(), autoMapping.simpleDemand());
        }

        @SuppressWarnings("unchecked")
        public EntityMapper<?, ?> buildEntityMapper(Class<?> sourceClass, Class<?> targetClass, Class<EntityMapperHandler<?, ?>> handlerClass, String groupKey, String[] simpleDemand) {
            EntityMapper<Object, Object> mapper = (EntityMapper<Object, Object>) buildEntityMapper(sourceClass, targetClass);
            EntityMapperHandler<Object, Object> entityMapperHandler = (EntityMapperHandler<Object, Object>) EntityManager.newEntityInstance(handlerClass);
            Group<Object, Object> group1 = Demands.group(groupKey);
            for (String demand : simpleDemand) {
                analysisSimpleDemand(group1, demand);
            }
            entityMapperHandler.handle(group1);
            mapper.want(group1);
            return mapper;
        }

        private <O, T> EntityMapper<O, T> buildEntityMapper(Class<O> sourceClass, Class<T> targetClass) {
            final Constructor<O> sourceConstructor;
            try {
                sourceConstructor = sourceClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法构造实体:" + sourceClass + ",请确认是否拥有无参数构造器");
            }
            final Constructor<T> targetConstructor;
            try {
                targetConstructor = targetClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法构造实体:" + targetClass + ",请确认是否拥有无参数构造器");
            }
            return EntityMapper.getMapper(() -> {
                try {
                    return sourceConstructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("无法构造实体:" + sourceClass + ",请确认是否拥有无参数构造器");
                }
            }, () -> {
                try {
                    return targetConstructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("无法构造实体:" + targetClass + ",请确认是否拥有无参数构造器");
                }
            }).group();
        }

        private void analysisSimpleDemand(DemandReceiver<Object, Object> receiver, String expression) {
            if (expression.matches("^!([a-zA-Z_$][a-zA-Z0-9_$]+)$")) {
                receiver.want(analysisExclude(expression));
            } else if (expression.matches("^([a-zA-Z_$][a-zA-Z0-9_$]+)(->)([a-zA-Z_$][a-zA-Z0-9_$]+)$")) {
                receiver.want(analysisMove(expression));
            } else {
                throw new RuntimeException("错误的表达式格式");
            }
        }

        /**
         * id->key  move
         * !key     exclude
         */
        private Demand<Object, Object> analysisMove(String expression) {
            String[] moveMapping = Arrays.stream(expression.split("->"))
                    .filter(s -> !s.isEmpty() || !s.isBlank())
                    .toArray(String[]::new);
            if (moveMapping.length != 2) {
                throw new RuntimeException("错误的表达式格式");
            }
            return Demands.move(moveMapping[0], moveMapping[1]);
        }

        private Demand<Object, Object> analysisExclude(String expression) {
            String remove = expression.substring(1);
            return Demands.excludeAuto(remove);
        }

        @Override
        public <O, T> EntityMapper<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz) {
            return mapper(sourceClazz, targetClazz, GroupEntityMapper.DEF_GROUP);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <O, T> EntityMapper<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz, Object groupKey) {
            MapperKey mapperKey = new MapperKey(sourceClazz, targetClazz);
            EntityMapper<?, ?> entityMapper = entityMappers.get(mapperKey);
            if (entityMapper == null) {
                EntityMapper<O, T> otEntityMapper = buildEntityMapper(sourceClazz, targetClazz);
                addMapper(otEntityMapper);
                return otEntityMapper;
            }
            return (EntityMapper<O, T>) entityMapper.group().obtain(groupKey);
        }


        @Override
        public <O, T> void addMapper(String groupKey, EntityMapper<O, T> entityMapper) {
            addMapper(groupKey, entityMapper, new DefEntityMapperHandler<>());
        }

        @SuppressWarnings("unchecked")
        public <O, T> void addMapper(Object groupKey, EntityMapper<O, T> entityMapper, EntityMapperHandler<O, T> handler) {
            MapperKey mapperKey = new MapperKey(entityMapper.sourceClass, entityMapper.targetClass);
            EntityMapper<O, T> entityMapper1 = (EntityMapper<O, T>) entityMappers.get(mapperKey);
            if (entityMapper1 != null) {
                entityMapper1.want(entityMapper.group(groupKey));
            } else {
                GroupEntityMapper<O, T> group = entityMapper.group();
                handler.handle(group);
                entityMappers.put(mapperKey, group);
            }
        }

        public <O, T> void addMapper(EntityMapper<O, T> entityMapper) {
            addMapper(GroupEntityMapper.DEF_GROUP, entityMapper);
        }

    }

    static {
        EntityMapperPool pool1;
        Properties properties = new Properties();
        try (InputStream is = EntityMapperTools.class.getClassLoader().getResourceAsStream("entity-mapper.properties")) {
            properties.load(is);
            String o = properties.getProperty(POOL_CLASS);
            String s = properties.getProperty(SCAN_ROOT);
            pool1 = (EntityMapperPool) EntityManager.newEntityInstance(Class.forName(o), s);
        } catch (IOException | NullPointerException ignored) {
            String s = properties.getProperty(SCAN_ROOT);
            pool1 = new DefaultEntityMapperPool(s);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("找不到配置内的池类型");
        }
        pool = pool1;
    }

    private record MapperKey(Class<?> sourceClass, Class<?> targetClass) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MapperKey mapperKey)) return false;
            return Objects.equals(sourceClass, mapperKey.sourceClass) && Objects.equals(targetClass, mapperKey.targetClass);
        }
    }
}
