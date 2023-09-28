package xyz.fmcy.util.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author 付高宏
 * @date 2023/1/29 14:35
 */
public final class Group<O, T> implements Demand<O, T>,DemandReceiver<O,T> {
    private final Object key;
    private final List<Demand<O, T>> demands;

    {
        demands = new ArrayList<>();
    }

    public Group(Object key) {
        this.key = key;
    }

    public Group<O,T> want(Demand<O,T> demand){
        demands.add(demand);
        return this;
    }

    @Override
    public Group<O,T> want(BiFunction<O, T, T> function) {
        demands.add(function::apply);
        return this;
    }

    public Object getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Group{" +
                "key=" + key +
                ", demands=" + demands +
                '}';
    }

    public List<Demand<O, T>> getDemands() {
        return demands;
    }

    @Override
    public void perform(O o, T t) {
        demands.forEach(demand -> demand.perform(o, t));
    }
}
