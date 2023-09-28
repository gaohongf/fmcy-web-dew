package xyz.fmcy.util.entity;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author 付高宏
 * @date 2023/1/23 14:40
 */
public class DataMoveDemand<O, T, D> implements Demand<O, T>, DataMapper<O, T, D> {
    private Function<O, D> dataGetter;
    private BiConsumer<T, D> dataSetter;
    @Override
    public void perform(O o, T t) {
        dataSetter.accept(t, dataGetter.apply(o));
    }
    @Override
    public Demand<O, T> mapping(Function<O, D> dataGetter, BiConsumer<T, D> dataSetter) {
        this.dataGetter = dataGetter;
        this.dataSetter = dataSetter;
        return this;
    }

    DataMoveDemand() {
    }

}
