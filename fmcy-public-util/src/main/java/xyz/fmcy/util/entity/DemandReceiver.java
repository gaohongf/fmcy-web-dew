package xyz.fmcy.util.entity;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author 付高宏
 * @date 2023/1/31 14:03
 */
public interface DemandReceiver<O, T> {
    default Predicate<String> fieldFilter() {
        return Optional.ofNullable(getDemands()).map(demands -> demands.stream()
                .filter(Group.class::isInstance)
                .map(demand -> (Group<O, T>) demand)
                .map(DemandReceiver::fieldFilter)
                .reduce(Predicate::and)
                .orElse($ -> true)
                .and(name -> !demands.stream()
                        .filter(ExcludeAutoField.class::isInstance)
                        .map(ExcludeAutoField.class::cast)
                        .map(ExcludeAutoField::exclude)
                        .toList()
                        .contains(name)
                )).orElseGet(() -> f -> true);
    }

    List<Demand<O, T>> getDemands();

    DemandReceiver<O, T> want(Demand<O, T> demand);

    DemandReceiver<O, T> want(BiFunction<O, T, T> function);
}
