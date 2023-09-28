package xyz.fmcy.util.entity;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author 付高宏
 * @date 2023/1/29 16:20
 */

public interface EntityManager{

    static <T> T newEntityInstance(Class<T> clazz, Object... objects) {
        try {
            return clazz.getDeclaredConstructor(Arrays.stream(objects)
                    .filter(Objects::nonNull)
                    .map(Object::getClass)
                    .toArray(Class[]::new)
            ).newInstance(objects);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("不要胡乱去用这个方法创建对象,你要先知道这个类的构造方法有什么,再考虑参数的传递,报这个错多半是参数乱传的锅");
        }
    }

}
