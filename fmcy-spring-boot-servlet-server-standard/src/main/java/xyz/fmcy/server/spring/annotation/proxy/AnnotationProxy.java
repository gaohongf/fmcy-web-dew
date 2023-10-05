package xyz.fmcy.server.spring.annotation.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解代理类
 *
 * @param <T>
 */
public class AnnotationProxy<T extends Annotation> implements InvocationHandler {
    private final Map<String, Object> values = new ConcurrentHashMap<>();
    private final Class<T> annotationClass;

    public AnnotationProxy(Class<T> annotationClass) {
        this.annotationClass = Objects.requireNonNull(annotationClass, "annotation class is null");
        values.putAll(getAnnotationDefaultValues(annotationClass));
    }

    @SuppressWarnings("unchecked")
    public AnnotationProxy(T annotation) {
        this(Objects.requireNonNull((Class<T>) annotation.annotationType(), "annotation is null"));
        getAnnotationValues(annotation).forEach(this::modify);
    }

    public static <T extends Annotation> AnnotationProxy<T> proxy(Class<T> annotationClass) {
        return new AnnotationProxy<>(annotationClass);
    }

    public static <T extends Annotation> AnnotationProxy<T> proxy(T annotation) {
        return new AnnotationProxy<>(annotation);
    }

    /**
     * 生成由注解默认值组成的Map
     * @param annotationClass 注解类
     */
    public static Map<String, Object> getAnnotationDefaultValues(Class<?> annotationClass) {
        Map<String, Object> map = new HashMap<>();
        Method[] methods = annotationClass.getDeclaredMethods();
        Arrays.stream(methods).forEach(method -> {
            Object defaultValue = method.getDefaultValue();
            if (defaultValue != null) {
                map.put(method.getName(), defaultValue);
            }
        });
        return map;
    }
    /**
     * 根据一个注解对象生成其值与方法的映射
     */
    public static Map<String, Object> getAnnotationValues(Annotation annotation) {
        Map<String, Object> map = new HashMap<>();
        Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .forEach((method) -> {
                    String name = method.getName();
                    method.setAccessible(true);
                    try {
                        map.put(name, method.invoke(annotation));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        Object defaultValue = method.getDefaultValue();
                        if (defaultValue != null) {
                            map.put(name, defaultValue);
                        }
                    }
                });
        return new HashMap<>(map);
    }

    public Map<String, Object> getValues() {
        return new HashMap<>(values);
    }

    public AnnotationProxy<T> modify(String key, Object value) {
        values.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Class<?> clazz = obj.getClass();
        if (clazz == annotationClass
                || (Proxy.isProxyClass(clazz)
                && ((AnnotationProxy<T>) Proxy.getInvocationHandler(obj)).annotationClass == annotationClass)) {
            return values.isEmpty() || values.entrySet().stream()
                    .anyMatch(entry -> {
                        try {
                            Method method = clazz.getMethod(entry.getKey());
                            method.setAccessible(true);
                            return method.invoke(obj).equals(entry.getValue());
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                            return false;
                        }
                    });
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        return (T) Proxy.newProxyInstance(AnnotationProxy.class.getClassLoader(), new Class[]{annotationClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String name = method.getName();
        return switch (name) {
            case "toString" -> AnnotationProxy.this.toString();
            case "hashCode" -> AnnotationProxy.this.hashCode();
            case "equals" -> AnnotationProxy.this.equals(args[0]);
            case "annotationType" -> AnnotationProxy.this.annotationClass;
            default -> values.get(name);
        };
    }
}
