package xyz.fmcy.server.spring.core;

import java.util.Map;
import java.util.function.Function;

public enum BaseDataTypeCast {
    TO_String(String::valueOf),
    TO_Integer(serializable -> Integer.valueOf(String.valueOf(serializable))),
    TO_Double(serializable -> Double.valueOf(String.valueOf(serializable))),
    TO_Short(serializable -> Short.valueOf(String.valueOf(serializable))),
    TO_Long(serializable -> Long.valueOf(String.valueOf(serializable))),
    TO_Boolean(serializable -> Integer.valueOf(String.valueOf(serializable))),
    TO_Float(serializable -> Float.valueOf(String.valueOf(serializable))),
    TO_Byte(serializable -> Byte.valueOf(String.valueOf(serializable))),
    TO_Char(serializable -> {
        String s = String.valueOf(serializable);
        if (s.length() != 1) {
            throw new ClassCastException("%s类型无法转为%s".formatted(serializable.getClass(), Character.class));
        }
        return s.charAt(0);
    });

    public static final Map<Class<?>, BaseDataTypeCast> castableMap = Map.ofEntries(
            Map.entry(byte.class, TO_Byte),
            Map.entry(short.class, TO_Short),
            Map.entry(int.class, TO_Integer),
            Map.entry(long.class, TO_Long),
            Map.entry(boolean.class, TO_Boolean),
            Map.entry(float.class, TO_Float),
            Map.entry(double.class, TO_Double),
            Map.entry(char.class, TO_Char),
            Map.entry(String.class, TO_String),
            Map.entry(Byte.class, TO_Byte),
            Map.entry(Short.class, TO_Short),
            Map.entry(Integer.class, TO_Integer),
            Map.entry(Long.class, TO_Long),
            Map.entry(Boolean.class, TO_Boolean),
            Map.entry(Float.class, TO_Float),
            Map.entry(Double.class, TO_Double),
            Map.entry(Character.class, TO_Char)
    );

    <T> BaseDataTypeCast(Function<Object, T> cast) {
        this.cast = cast;
    }

    public final Function<Object, ?> cast;

    public static boolean catCast(Class<?> clazz) {
        return castableMap.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o, Class<T> target) {
        if (catCast(o.getClass()) && catCast(target)) {
            return (T) castableMap.get(target).cast.apply(o);
        } else {
            throw new ClassCastException(o.getClass().getName() + "不能转换为: " + target);
        }
    }
}
