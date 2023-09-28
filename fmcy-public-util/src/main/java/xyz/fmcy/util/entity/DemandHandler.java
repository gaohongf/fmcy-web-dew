package xyz.fmcy.util.entity;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author 付高宏
 * @date 2023/1/31 17:07
 */
public interface DemandHandler<O, T> extends Grouped {

    Class<O> getSourceClass();

    Class<T> getTargetClass();

    T wiseMapping(O o);

    T wiseMapping();

    T wiseMapping(O o, Supplier<T> supplier);

    T wiseMapping(O o, T t);

    T wiseMapping(O o, T t, boolean auto);

    T wiseMapping(O o, Supplier<T> supplier, boolean auto);

    T wiseMapping(O o, boolean auto);

    <C extends Collection<T>, I extends Iterable<O>> C mapAll(I sources, Supplier<C> supplier);

    <C extends Collection<T>, I extends Iterable<O>> C mapAll(Supplier<T> targetBuilder, I sources, Supplier<C> supplier);

    <I extends Iterable<O>> List<T> mapAll(Supplier<T> targetBuilder, I sources);

    <I extends Iterable<O>> List<T> mapAll(I sources);

    DemandHandler<O, T> groupGet(Object key);
}
