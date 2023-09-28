package xyz.fmcy.util.entity;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author 付高宏
 * @date 2023/1/23 15:18
 */
@FunctionalInterface
public interface DataMapper<O, T, D> {
    Demand<O, T> mapping(Function<O, D> dataGetter, BiConsumer<T, D> dataSetter);
}
