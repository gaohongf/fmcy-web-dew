package xyz.fmcy.server.spring.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 必须是声明的注解不支持多重代理
 *
 * @param <T>
 */
public class AnnotationProxy<T extends Annotation> implements InvocationHandler {
    private final Map<String, Object> defaultValues = new ConcurrentHashMap<>();
    private final Class<T> annotationClass;

    public AnnotationProxy(Class<T> annotationClass) {
        this.annotationClass = Objects.requireNonNull(annotationClass, "annotation class is null");
        defaultValues.putAll(getAnnotationDefaultValues(annotationClass));
    }

    @SuppressWarnings("unchecked")
    public AnnotationProxy(T annotation) {
        this(Objects.requireNonNull((Class<T>) annotation.annotationType(), "annotation is null"));
        getAnnotationValues(annotationClass, annotation).forEach(this::modify);
    }

    public static <T extends Annotation> AnnotationProxy<T> proxy(Class<T> annotationClass) {
        return new AnnotationProxy<>(annotationClass);
    }

    public static <T extends Annotation> AnnotationProxy<T> proxy(T annotation) {
        return new AnnotationProxy<>(annotation);
    }

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

    public static Map<String, Object> getAnnotationValues(Class<?> annotationClass, Annotation annotation) {
        Map<String, Object> map = new HashMap<>();
        Method[] methods = annotationClass.getDeclaredMethods();
        Arrays.stream(methods)
                .filter(method -> Arrays.stream(annotation.annotationType().getDeclaredMethods())
                        .map(Method::getName)
                        .toList().contains(method.getName()))
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

    public Map<String, Object> getDefaultValues() {
        return new HashMap<>(defaultValues);
    }

    public AnnotationProxy<T> modify(String key, Object value) {
        defaultValues.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return defaultValues.toString();
    }

    @Override
    public int hashCode() {
        return defaultValues.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        Class<?> clazz = obj.getClass();
        if (clazz == annotationClass
                || (Proxy.isProxyClass(clazz)
                && ((AnnotationProxy<T>) Proxy.getInvocationHandler(obj)).annotationClass == annotationClass)) {
            return defaultValues.isEmpty() || defaultValues.entrySet().stream()
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object defaultValue = defaultValues.get(name);
        if (defaultValue != null) return defaultValue;
        else if (name.equals("toString")) return AnnotationProxy.this.toString();
        else if (name.equals("hashCode")) return AnnotationProxy.this.hashCode();
        else if (name.equals("equals")) return AnnotationProxy.this.equals(args[0]);
        else if (name.equals("annotationType")) return annotationClass;
        return null;
    }
}
