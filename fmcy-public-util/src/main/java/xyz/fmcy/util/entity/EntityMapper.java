package xyz.fmcy.util.entity;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author 付高宏
 * @date 2023/1/23 14:39
 */
public class EntityMapper<O, T> implements GroupManager<O, T>,
        EntityManager, DemandReceiver<O, T>, DemandHandler<O, T> {
    /**
     * 原实体类
     */
    public final Class<O> sourceClass;
    /**
     * 目标实体类
     */
    public final Class<T> targetClass;
    protected final Supplier<O> sourceTemplate;
    protected final Supplier<T> targetTemplate;
    protected Field[] sourceFields;
    protected Field[] targetFields;

    protected Map<Field, Field> fieldMapping;
    /**
     * 转换需求
     */
    private final List<Demand<O, T>> demands = new ArrayList<>();

    public EntityMapper(Class<O> sourceClass,
                        Class<T> targetClass,
                        Supplier<O> sourceTemplate,
                        Supplier<T> targetTemplate) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
        this.sourceTemplate = sourceTemplate;
        this.targetTemplate = targetTemplate;
    }

    public List<Demand<O, T>> getDemands() {
        return demands;
    }

    @Override
    public Class<O> getSourceClass() {
        return sourceClass;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    /**
     * 构造原实体对象
     *
     * @return 原实体对象
     */
    public O buildSource() {
        return sourceTemplate.get();
    }

    /**
     * 构造目标实体对象
     *
     * @return 目标实体对象
     */
    public T buildTarget() {
        return targetTemplate.get();
    }

    /**
     * 增加一个需求
     *
     * @param demand 需求
     * @return this
     */
    public EntityMapper<O, T> want(Demand<O, T> demand) {
        this.demands.add(Objects.requireNonNull(demand, "Demand为空"));
        return this;
    }

    public EntityMapper<O, T> want(BiFunction<O, T, T> function) {
        this.demands.add(function::apply);
        return this;
    }

    /**
     * 执行目标需求
     *
     * @param o 原实体对象
     * @param t 目标实体对象
     * @return 目标实体对象
     */
    protected T perform(O o, T t) {
        demands.forEach(demand -> demand.perform(o, t));
        return t;
    }

    protected void fieldCache() {
        if (sourceFields == null) {
            sourceFields = sourceClass.getDeclaredFields();
        }
        if (targetFields == null) {
            targetFields = targetClass.getDeclaredFields();
        }
        if (fieldMapping == null) {
            Map<String, Field> sourceNames = Arrays.stream(sourceFields)
                    .collect(Collectors.toMap(Field::getName, f -> f));
            fieldMapping = Arrays.stream(targetFields).filter(field -> sourceNames.get(field.getName()) != null)
                    .collect(Collectors.toMap(f -> sourceNames.get(f.getName()), f -> f));
        }
    }

    /**
     * 自动写入同名属性
     *
     * @param o 原实体对象
     * @param t 目标实体对象
     * @return 目标实体对象
     */
    protected T autoWrite(O o, T t) {
        fieldCache();
        Predicate<String> predicate = fieldFilter();
        Arrays.stream(sourceFields).filter(f -> predicate.test(f.getName()))
                .forEach(field ->
                        Demands.dataSetForField(t, Demands.dataGetForField(o, field), fieldMapping.get(field))
                );
        return t;
    }

    protected <M extends Map<O, T>> M autoWrite(M m) {
        fieldCache();
        Predicate<String> predicate = fieldFilter();
        Arrays.stream(sourceFields).filter(f -> predicate.test(f.getName()))
                .forEach(field -> m.forEach((o, t) -> Demands.dataSetForField(t, Demands.dataGetForField(o, field), fieldMapping.get(field))));
        return m;
    }

    protected <M extends List<Map.Entry<O, T>>> M autoWrite(M m) {
        fieldCache();
        Predicate<String> predicate = fieldFilter();
        Arrays.stream(sourceFields).filter(f -> predicate.test(f.getName()))
                .forEach(field -> m.forEach((entry) -> Demands.dataSetForField(entry.getValue(), Demands.dataGetForField(entry
                        .getKey(), field), fieldMapping.get(field))
                ));
        return m;
    }

    /**
     * 按照约定映射属性
     *
     * @param o         原实体对象
     * @param t         目标实体对象
     * @param blueprint 蓝图
     * @return 目标实体对象
     */
    public T fastMapping(O o, T t, BiFunction<O, T, T> blueprint) {
        return blueprint.apply(o, t);
    }

    /**
     * 创建目标对象,并且将对象的属性映射到目标对象中
     *
     * @param o 原实体对象
     * @return 目标实体对象
     */
    public T wiseMapping(O o) {
        return wiseMapping(o, buildTarget());
    }

    public T wiseMapping() {
        return wiseMapping(buildSource(), buildTarget());
    }

    public EntityMapper<O, T> excludeAuto(String fieldName) {
        this.want(Demands.excludeAuto(fieldName));
        return this;
    }

    /**
     * 获取目标对象,并且将原对象的属性映射到目标对象中
     *
     * @param o        原实体对象
     * @param supplier 目标对象的提供者
     * @return 目标实体对象
     */
    public T wiseMapping(O o, Supplier<T> supplier) {
        return wiseMapping(o, supplier.get());
    }

    /**
     * 将原对象的属性映射到目标对象
     *
     * @param o 原实体对象
     * @param t 目标实体对象
     * @return 目标实体对象
     */
    public T wiseMapping(O o, T t) {
        return wiseMapping(o, t, true);
    }

    /**
     * 将原对象的属性映射到目标对象,开启auto时将进行同名属性映射
     *
     * @param o    原实体对象
     * @param t    目标实体对象
     * @param auto 同名属性自动映射
     * @return 目标对象
     */
    public T wiseMapping(O o, T t, boolean auto) {
        if (auto) {
            autoWrite(o, t);
        }
        perform(o, t);
        return t;
    }

    /**
     * 获取目标对象,并且将原对象的属性映射到目标对象中,开启auto时将进行同名属性映射
     *
     * @param o        原实体对象
     * @param supplier 目标对象的提供者
     * @return 目标对象
     */
    public T wiseMapping(O o, Supplier<T> supplier, boolean auto) {
        return wiseMapping(o, supplier.get(), auto);
    }

    /**
     * 创建目标对象,并且将对象的属性映射到目标对象中,开启auto时将进行同名属性映射
     *
     * @param o    原实体对象
     * @param auto 目标实体对象
     * @return 目标实体对象
     */
    public T wiseMapping(O o, boolean auto) {
        return wiseMapping(o, buildTarget(), auto);
    }

    public <C extends Collection<T>, I extends Iterable<O>> C mapAll(I sources, Supplier<C> supplier) {
        return mapAll(targetTemplate, sources, supplier);
    }

    public <C extends Collection<T>, I extends Iterable<O>> C mapAll(Supplier<T> targetBuilder, I sources, Supplier<C> supplier) {
        C targets = supplier.get();
        List<Map.Entry<O, T>> entries = new ArrayList<>();
        sources.forEach(source -> {
            if (source == null) return;
            T t = targetBuilder.get();
            targets.add(t);
            entries.add(Map.entry(source, t));
        });
        autoWrite(entries).forEach(entry -> wiseMapping(entry.getKey(), entry.getValue(), false));
        return targets;
    }

    public <I extends Iterable<O>> List<T> mapAll(Supplier<T> targetBuilder, I sources) {
        return mapAll(targetBuilder, sources, ArrayList::new);
    }

    public <I extends Iterable<O>> List<T> mapAll(I sources) {
        return mapAll(sources, ArrayList::new);
    }

    @Override
    public DemandHandler<O, T> groupGet(Object key) {
        return group().obtain(key);
    }

    public EntityMapper<O, T> removeDemand(int index) {
        demands.remove(index);
        return this;
    }

    public EntityMapper<O, T> removeDemand(Demand<O, T> demand) {
        demands.remove(demand);
        return this;
    }


    @SuppressWarnings("unchecked")
    public static <O, T> EntityMapper<O, T> getMapper(Supplier<O> sourceTemplate,
                                                      Supplier<T> targetTemplate) {
        return new EntityMapper<>(
                (Class<O>) Objects.requireNonNull(sourceTemplate.get()).getClass(),
                (Class<T>) Objects.requireNonNull(targetTemplate.get()).getClass(),
                sourceTemplate,
                targetTemplate);
    }

    public static <O, T> EntityMapper<O, T> getMapper(Class<O> sourceClass, Class<T> targetClass) {
        return getMapper(
                () -> EntityManager.newEntityInstance(sourceClass),
                () -> EntityManager.newEntityInstance(targetClass)
        );
    }


    @Override
    public Group<O, T> group(Object key) {
        Group<O, T> group = new Group<>(key);
        demands.forEach(group::want);
        return group;
    }

    public GroupEntityMapper<O, T> group() {
        return new GroupEntityMapper<>(this);
    }


    public GroupEntityMapper<O, T> group(Object key, boolean setDefGroup) {
        if (setDefGroup) {
            return new GroupEntityMapper<>(this).want(this.group(key));
        } else {
            return new GroupEntityMapper<>(sourceClass, targetClass, sourceTemplate, targetTemplate)
                    .want(this.group(key));
        }
    }
}
