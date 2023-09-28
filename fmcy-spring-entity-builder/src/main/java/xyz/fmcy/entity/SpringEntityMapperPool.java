package xyz.fmcy.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionException;
import org.springframework.lang.Nullable;
import xyz.fmcy.entity.annotation.EM;
import xyz.fmcy.entity.annotation.EMS;
import xyz.fmcy.util.auto.PackageScanner;
import xyz.fmcy.util.entity.*;
import xyz.fmcy.util.entity.auto.EntityMapperPool;
import xyz.fmcy.util.entity.auto.EntityMapperHandler;
import xyz.fmcy.util.entity.auto.DefEntityMapperHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Configuration("entityMapperPool")
public class SpringEntityMapperPool implements EntityMapperPool, EMParser {
    private final DemandExpressionRoot root;
    private final Logger logger = LoggerFactory.getLogger(SpringEntityMapperPool.class);

    private record MapperKey(Class<?> sourceClass, Class<?> targetClass) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MapperKey mapperKey)) return false;
            return Objects.equals(sourceClass, mapperKey.sourceClass)
                    && Objects.equals(targetClass, mapperKey.targetClass);
        }
    }

    private final Map<MapperKey, EntityMapper<Object, Object>> mappers = new ConcurrentHashMap<>();
    private final GenericApplicationContext applicationContext;

    public SpringEntityMapperPool(GenericApplicationContext applicationContext,
                                  @Nullable SimpleClassNameMap simpleClassNameMap,
                                  @Qualifier("entityMapperScanPackages") @Nullable String[] entityMapperScanPackages) {
        this.applicationContext = applicationContext;
        if (simpleClassNameMap != null) {
            this.root = new DemandExpressionRoot(applicationContext, simpleClassNameMap.getClassMap());
        } else {
            this.root = new DemandExpressionRoot(applicationContext);
        }
        List<String> packages = Arrays.stream(Objects.requireNonNullElseGet(entityMapperScanPackages, () -> new String[0])).toList();
        PackageScanner.build(this::classScan).filter(PackageScanner.hasAnnotation(EM.class)
                        .or(PackageScanner.hasAnnotation(EMS.class)))
                .excludeClass("xyz.fmcy.util.entity.auto.EntityMapperTools")
                .scanPacket(packages.toArray(new String[0]));

    }

    private void classScan(Class<?> clazz) {
        EM em = clazz.getAnnotation(EM.class);
        if (em != null) {
            runMapping(clazz, em);
        }
        EMS ems = clazz.getAnnotation(EMS.class);
        if (ems != null) {
            Arrays.stream(ems.value()).forEach(e -> runMapping(clazz, e));
        }
    }

    public void runMapping(Class<?> source, EM em) {
        Class<?> target = em.target();
        EM.Mapping becomeOf = em.becomeOf();
        EM.Mapping reverse = em.reverse();
        if (becomeOf.enable()) {
            mapping(source, target, becomeOf);
        }
        if (reverse.enable()) {
            mapping(target, source, reverse);
        }
    }

    @SuppressWarnings("unchecked")
    private void mapping(Class<?> source, Class<?> target, EM.Mapping mapping) {
        try {
            EntityMapper<Object, Object> mapper = (EntityMapper<Object, Object>) createMapper(source, target);
            wantSimpleDemands(mapper, mapping.demands());
            EntityMapperHandler<Object, Object> handler = null;
            try {
                handler = applicationContext.getBean(mapping.handler());
            } catch (BeansException e) {
                logger.error(e.getMessage());
            }
            addMapper(mapping.group().groupKey(), mapper, handler);
        } catch (NoSuchMethodException e) {
            logger.warn("在类:" + source + "或者类:" + target + "中找不到无参数构造方法");
        }
    }

    private EntityMapper<?, ?> createMapper(Class<?> sourceClass, Class<?> targetClass) throws NoSuchMethodException {
        return EntityMapper.getMapper(sourceClass, targetClass);
    }

    private <O, T> void handle(DemandReceiver<O, T> receiver, EntityMapperHandler<O, T> entityMapperHandler) {
        if (entityMapperHandler != null) entityMapperHandler.handle(receiver);
    }

    private void wantSimpleDemands(DemandReceiver<Object, Object> receiver, String[] demands) {
        Map<String, Class<?>> classMap = new ConcurrentHashMap<>();
        Arrays.stream(demands).forEach(demand -> {
            if (demand.matches("^\\S+ is \\S+$")) {
                String[] classMapping = demand.split(" is ");
                if (classMapping.length != 2) {
                    throw new ExpressionException("表达式错误:" + demand);
                }
                try {
                    classMap.put(classMapping[0], Class.forName(classMapping[1]));
                } catch (ClassNotFoundException e) {
                    throw new ExpressionException("表达式错误:" + demand);
                }
                return;
            }
            Demand<Object, Object> demand1 = getDemand(demand, classMap);
            if (demand1 != null) {
                receiver.want(demand1);
            } else {
                throw new ExpressionException("表达式错误:" + demand);
            }
        });
    }

    private Demand<Object, Object> getDemand(String demandStr, Map<String, Class<?>> classMap) {
        if (demandStr.matches("^[a-zA-Z0-9_$]+->[a-zA-Z0-9_$]+$")) { // fieldName1->fieldName2
            return parseMove(demandStr);
        } else if (demandStr.matches("^![a-zA-Z0-9_$]+$")) {    // !fieldName
            return parseExclude(demandStr);
        } else if (demandStr.matches(
                "^(\\{?\\[?\\^?)?[\\w$@]+}?]?(->[\\S ]+)+$"
        )) {
            return parseMapsMove(demandStr, classMap);
        }
        throw new ExpressionException("表达式错误:" + demandStr);
    }

    private Demand<Object, Object> parseMapsMove(String str, Map<String, Class<?>> classMap) {
        String[] steps = str.split("->");
        String source = steps[0];
        String target = steps[steps.length - 1];
        Function<Object, Object> function = o -> o;
        for (int i = 1; i < steps.length - 1; i++) {
            function = analysisMiddle(steps[i], function, classMap);
        }
        return Demands.mapMove(analysisGet(source), function, analysisTail(target));
    }

    private Function<Object, Object> analysisGet(String exp) {
        if (exp.matches("^@$")) {
            return t -> t;
        } else if (exp.matches("^[a-zA-Z_$][\\w$]*$")) {
            return root.get(exp);
        } else if (exp.matches("^\\{[a-zA-Z_$][\\w$]*}$")) {
            return root.getMethod(exp, false);
        } else if (exp.matches("^\\{\\^[a-zA-Z_$][\\w$]*}$")) {
            return root.getMethod(exp, true);
        }
        throw new ExpressionException("表达式错误:" + exp);
    }

    private Function<Object, Object> analysisMiddle(String exp, Function<Object, Object> function, Map<String, Class<?>> classMap) {
        if (exp.matches("^@[\\S ]+$")) {
            function = function.andThen(root.change(exp));
        } else if (exp.matches("^\\*([a-zA-Z][a-zA-Z0-9$_]*(\\.[a-zA-Z][a-zA-Z0-9$_]*)*)$")) {
            function = function.andThen(root.cast(exp.replace("*", ""), classMap));
        } else if (exp.matches("^(\\{?\\^?)?[a-zA-Z_$][\\w$]*}?$")) {
            function = function.andThen(analysisGet(exp));
        } else if (exp.matches("^\\??([a-zA-Z][a-zA-Z0-9\\-_$]*\\.[a-zA-Z][a-zA-Z0-9_$]*)$")) {
            function = function.andThen(root.invokeBeanMethod(exp));
        } else if (exp.matches("\\??\\[[\\S\\s]+]")) {
            function = function.andThen(invokeMethod(exp, classMap));
        }
        return function;
    }

    private Function<Object, Object> invokeMethod(String exp, Map<String, Class<?>> classMap) {
        if (exp.matches("^\\??\\[[\\w$]+]$")) { //调用普通方法
            return root.invokeObjMethod(exp);
        } else if (exp.matches("^\\??\\[[a-zA-Z_$][\\S ]+$")) { //普通方法指定参数
            return root.invokeObjArgsMethod(exp);
        } else if (exp.matches("^\\??\\[\\^[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*\\.[a-zA-Z_$][\\S ]+]$")) { //静态方法指定参数
            return root.invokeStaticArgsMethod(exp, classMap);
        } else if (exp.matches("^\\??\\[\\^[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*]$")) { //调用静态方法
            return root.invokeStaticMethod(exp, classMap);
        }
        throw new ExpressionException("表达式错误:" + exp);
    }

    private BiConsumer<Object, Object> analysisTail(String exp) {
        if (exp.matches("^[a-zA-Z_$][\\w$]*$")) {
            return root.set(exp);
        } else if (exp.matches("^\\{[a-zA-Z_$][\\w$]*}$")) {
            return root.setMethod(exp, false);
        } else if (exp.matches("^\\{\\^[a-zA-Z_$][\\w$]*}$")) {
            return root.setMethod(exp, true);
        }
        throw new ExpressionException("表达式错误:" + exp);
    }

    private Demand<Object, Object> parseMove(String str) {
        String[] twoFields = str.split("->");
        return root.move(twoFields[0], twoFields[1]);
    }

    private Demand<Object, Object> parseExclude(String str) {
        String replace = str.replace("!", "");
        return root.exclude(replace);
    }


    @Override
    public <O, T> DemandHandler<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz) {
        return mapper(sourceClazz, targetClazz, GroupEntityMapper.DEF_GROUP);
    }

    @SuppressWarnings("unchecked")
    public <O, T> DemandHandler<O, T> mapper(Class<O> sourceClazz, Class<T> targetClazz, Object groupKey) {
        EntityMapper<Object, Object> mapper = mappers.get(new MapperKey(sourceClazz, targetClazz));
        if (mapper == null) return null;
        return (DemandHandler<O, T>) mapper.group().obtain(groupKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <O, T> void addMapper(String groupKey, EntityMapper<O, T> entityMapper) {
        addMapper(groupKey, entityMapper, applicationContext.getBean(DefEntityMapperHandler.class));
    }

    @SuppressWarnings("unchecked")
    public <O, T> void addMapper(String groupKey, EntityMapper<O, T> entityMapper, EntityMapperHandler<O, T> handler) {
        MapperKey key = new MapperKey(entityMapper.sourceClass, entityMapper.targetClass);
        Optional.ofNullable((EntityMapper<O, T>) mappers.get(key))
                .ifPresentOrElse(mapper -> mapper.want(entityMapper.group(groupKey)), () -> {
                    handle(entityMapper, handler);
                    GroupEntityMapper<O, T> group = entityMapper.group(groupKey, false);
                    mappers.put(key, (EntityMapper<Object, Object>) group);
                });
    }
}
