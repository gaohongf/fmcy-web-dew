package xyz.fmcy.util.pu;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Optionals {
    private Optionals() {
    }

    public static <E, O> O mapElse(E e, Function<E, O> mapper, O object) {
        return Optional.ofNullable(e)
                .map(mapper)
                .orElse(object);
    }

    public static <E, O> O mapElseGet(E e, Function<E, O> mapper, Supplier<O> supplier) {
        return mapElse(e, mapper, Objects.requireNonNull(supplier).get());
    }

    public static <E, O> O mapElseNull(E e, Function<E, O> mapper) {
        return mapElse(e, mapper, null);
    }

}
