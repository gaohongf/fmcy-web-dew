package xyz.fmcy.util.entity;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author 付高宏
 * @date 2023/1/23 15:10
 */
public class Demands {

    public static <O, T, D> DataMapper<O, T, D> move() {
        return new DataMoveDemand<>();
    }

    public static <O, T, D> Demand<O, T> move(Function<O, D> dataGetter, BiConsumer<T, D> dataSetter) {
        return Demands.<O, T, D>move().mapping(dataGetter, dataSetter);
    }

    public static <O, T, D> Demand<O, T> move(String sourceFieldName, BiConsumer<T, D> dataSetter) {
        return Demands.<O, T, D>move().mapping((o) -> dataGetForName(o, sourceFieldName), dataSetter);
    }

    public static <O, T, D> Demand<O, T> move(Function<O, D> dataGetter, String targetFieldName) {
        return Demands.<O, T, D>move()
                .mapping(dataGetter, (t, d) -> dataSetForName(t, d, targetFieldName));
    }

    public static <O, T, D> Demand<O, T> move(String sourceFieldName, String targetFieldName) {
        return Demands.<O, T, D>move()
                .mapping((o) -> dataGetForName(o, sourceFieldName),
                        (t, d) -> dataSetForName(t, d, targetFieldName));
    }

    public static <O, T, D> Demand<O, T> move(Field sourceField, Field targetField) {
        return Demands.<O, T, D>move()
                .mapping((o) -> dataGetForField(o, sourceField),
                        (t, d) -> dataSetForField(t, d, targetField));
    }

    public static <O, T, R, D> Demand<O, T>
    mapMove(Function<O, D> dataGetter, Function<D, R> mapper, BiConsumer<T, R> dataSetter) {
        return move(dataGetter, (t, d) -> Optional
                .ofNullable(d)
                .map(mapper)
                .ifPresent(r -> dataSetter.accept(t, r))
        );
    }

    public static <O, T, R, D> Demand<O, T>
    mapMove(String sourceFieldName, Function<D, R> mapper, BiConsumer<T, R> dataSetter) {
        return mapMove((o) -> dataGetForName(o, sourceFieldName), mapper, dataSetter);
    }

    public static <O, T, R, D> Demand<O, T>
    mapMove(Function<O, D> dataGetter, Function<D, R> mapper, String targetFieldName) {
        return mapMove(dataGetter, mapper, (t, d) -> dataSetForName(t, d, targetFieldName));
    }

    public static <O, T, R, D> Demand<O, T>
    mapMove(String sourceFieldName, Function<D, R> mapper, String targetFieldName) {
        return mapMove((o -> dataGetForName(o, sourceFieldName)), mapper, targetFieldName);
    }

    public static <O, T, R, D> Demand<O, T>
    mapsMove(Function<O, D> dataGetter, Function<Optional<D>, Optional<R>> mappers, BiConsumer<T, R> dataSetter) {
        return move(dataGetter, (t, d) -> mappers.apply(Optional.ofNullable(d)).ifPresent(r -> dataSetter.accept(t, r)));
    }

    public static <O, T> Group<O, T> group(Object key) {
        return new Group<>(key);
    }

    public static <O, T> Demand<O, T> excludeAuto(String field) {
        return (ExcludeAutoField<O, T>) () -> field;
    }

    public static <T, D> void dataSetForName(T target, D data, String targetFieldName) {
        try {
            Field field = target.getClass().getDeclaredField(targetFieldName);
            dataSetForField(target, data, field);
        } catch (Exception ignored) {
        }
    }

    public static <T, D> void dataSetForField(T target, D data, Field field) {
        try {
            if (!field.canAccess(target)) {
                field.setAccessible(true);
            }
            if (!Objects.equals("serialVersionUID", field.getName())) {
                field.set(target, data);
            }
        } catch (Exception ignored) {
        }
    }

    public static <O, D> D dataGetForName(O o, String sourceFieldName) {
        try {
            Field field = o.getClass().getDeclaredField(sourceFieldName);
            return dataGetForField(o, field);
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <O, D> D dataGetForField(O o, Field field) {
        try {
            if (!field.canAccess(o)) {
                field.setAccessible(true);
            }
            if (!Objects.equals("serialVersionUID", field.getName())) {
                return (D) field.get(o);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
