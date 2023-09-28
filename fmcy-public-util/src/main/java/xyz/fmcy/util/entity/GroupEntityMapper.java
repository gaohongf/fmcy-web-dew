package xyz.fmcy.util.entity;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author 付高宏
 * @date 2023/1/29 14:45
 */
public class GroupEntityMapper<O, T> extends EntityMapper<O, T> {
    private final Map<Object, Group<O, T>> groups = new HashMap<>();

    public final static String DEF_GROUP = "def_group";

    public GroupEntityMapper(Class<O> sourceClass, Class<T> targetClass, Supplier<O> sourceTemplate, Supplier<T> targetTemplate) {
        super(sourceClass, targetClass, sourceTemplate, targetTemplate);
    }

    public GroupEntityMapper(EntityMapper<O, T> entityMapper) {
        super(entityMapper.sourceClass, entityMapper.targetClass, entityMapper.sourceTemplate, entityMapper.targetTemplate);
        Group<O, T> group = entityMapper.group(DEF_GROUP);
        this.groups.put(DEF_GROUP, group);
    }

    @Override
    public List<Demand<O, T>> getDemands() {
        return groups.get(DEF_GROUP).getDemands();
    }

    @Override
    public GroupEntityMapper<O, T> want(Demand<O, T> demand) {
        if (demand instanceof Group<O, T> group) {
            Object key = group.getKey();
            Group<O, T> otGroup = groups.get(key);
            if (otGroup != null){
                otGroup.want(group);
            }else {
                groups.put(key,group);
            }
        } else {
            want(DEF_GROUP, demand);
        }
        return this;
    }

    public GroupEntityMapper<O, T> want(Object key, Demand<O, T> demand) {
        want(Demands.<O, T>group(key).want(demand));
        return this;
    }

    @Override
    public GroupEntityMapper<O, T> want(BiFunction<O, T, T> function) {
        want(DEF_GROUP, function);
        return this;
    }

    public GroupEntityMapper<O, T> want(Object key, BiFunction<O, T, T> function) {
        want(key, (Demand<O, T>) function::apply);
        return this;
    }

    @Override
    protected T perform(O o, T t) {
        groups.get(DEF_GROUP).perform(o, t);
        return t;
    }

    @Override
    public T wiseMapping(O o, T t, boolean auto) {
        if (auto) {
            autoWrite(o, t);
        }
        groups.get(DEF_GROUP).perform(o, t);
        return t;
    }

    @Override
    public <C extends Collection<T>, I extends Iterable<O>> C mapAll(Supplier<T> targetBuilder, I sources, Supplier<C> supplier) {
        return super.mapAll(targetBuilder, sources, supplier);
    }

    @Override
    public GroupEntityMapper<O, T> group() {
        return this;
    }

    public EntityMapper<O, T> obtain(Object key) {
        var mapper = EntityMapper.getMapper(sourceTemplate, targetTemplate);
        Group<O, T> group = groups.get(key);
        if (group != null) mapper.want(group);
        else want(Demands.group(key));
        return mapper;
    }

    public EntityMapper<O, T> obtain() {
        return obtain(DEF_GROUP);
    }

    @SuppressWarnings("unchecked")
    public static <O, T> EntityMapper<O, T> getMapper(Supplier<O> sourceTemplate,
                                                      Supplier<T> targetTemplate) {
        return new GroupEntityMapper<>(
                (Class<O>) Objects.requireNonNull(sourceTemplate.get()).getClass(),
                (Class<T>) Objects.requireNonNull(targetTemplate.get()).getClass(),
                sourceTemplate,
                targetTemplate);
    }

}
