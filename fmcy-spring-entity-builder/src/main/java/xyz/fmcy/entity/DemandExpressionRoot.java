package xyz.fmcy.entity;

import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import xyz.fmcy.util.entity.Demand;
import xyz.fmcy.util.entity.Demands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DemandExpressionRoot {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final BeanFactoryResolver beanFactoryResolver;

    // b|B|bool|Bool|i|I|f|F|d|D|c|C|s|S|str
    private final Map<String, Class<?>> defaultClassMap = new ConcurrentHashMap<>(Map.ofEntries(
            entry("b", byte.class),
            entry("B", Byte.class),
            entry("bool", boolean.class),
            entry("Bool", Boolean.class),
            entry("i", int.class),
            entry("I", Integer.class),
            entry("f", float.class),
            entry("F", Float.class),
            entry("d", double.class),
            entry("D", Double.class),
            entry("c", char.class),
            entry("C", Character.class),
            entry("s", short.class),
            entry("S", Short.class),
            entry("l", long.class),
            entry("L", Long.class),
            entry("o", Object.class),
            entry("str", String.class)
    ));

    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return Map.entry(k, v);
    }

    public DemandExpressionRoot(ApplicationContext applicationContext) {
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
    }

    public DemandExpressionRoot(ApplicationContext applicationContext, Map<String, Class<?>> classMap) {
        this(applicationContext);
        this.defaultClassMap.putAll(classMap);
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> cast(String type, Map<String, Class<?>> classMap) {
        return (d) -> {
            if (d == null) return null;
            Class<?> clazz = findStaticClass(type, classMap);
            String s = d.toString();
            if (clazz == String.class) {
                return (R) s;
            }
            if (clazz == boolean.class || clazz == Boolean.class) {
                return (R) Boolean.valueOf(s);
            }
            if (clazz == int.class || clazz == Integer.class) {
                return (R) Integer.valueOf(s);
            }
            if (clazz == short.class || clazz == Short.class) {
                return (R) Short.valueOf(s);
            }
            if (clazz == long.class || clazz == Long.class) {
                return (R) Long.valueOf(s);
            }
            if (clazz == byte.class || clazz == Byte.class) {
                return (R) Byte.valueOf(s);
            }
            if (clazz == float.class || clazz == Float.class) {
                return (R) Float.valueOf(s);
            }
            if (clazz == double.class || clazz == Double.class) {
                return (R) Double.valueOf(s);
            }
            if (clazz == char.class || clazz == Character.class) {
                return (R) Character.valueOf(Optional.of(s)
                        .filter(q -> q.length() != 1)
                        .orElseThrow(() -> new RuntimeException("无法转换为类型<char>")).charAt(0)
                );
            }
            throw new RuntimeException("类型:<" + d.getClass() + ">无法转换为<" + clazz + ">");
        };
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> change(String exp) {
        String value = exp.replaceAll("^@", "");
        return (d) -> {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setBeanResolver(beanFactoryResolver);
            context.setVariable("data", d);
            return (R) parser.parseExpression(value).getValue(context);
        };
    }

    public <D, R> Function<D, R> get(String field) {
        return (d) -> Demands.dataGetForName(d, field);
    }

    public <T, R> BiConsumer<T, R> set(String field) {
        return (t, d) -> Demands.dataSetForName(t, d, field);
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> getMethod(String field, boolean up) {
        String methodName;
        field = field.replaceAll("[{}^]", "");
        methodName = "get" + (up ? field.substring(0, 1).toUpperCase() + field.substring(1) : field) + "()";
        return (d) -> (R) parser.parseExpression(methodName).getValue(d);
    }

    public <R, T> BiConsumer<R, T> setMethod(String field, boolean up) {
        String methodName;
        field = field.replaceAll("[{}^]", "");
        methodName = "set" + (up ? field.substring(0, 1).toUpperCase() + field.substring(1) : field);
        return (r, t) -> parser.parseExpression(methodName + "('" + t + "')").getValue(r);
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> invokeBeanMethod(String beanMethod) {
        char c = beanMethod.charAt(0);
        boolean isPeek = Objects.equals(c, '?');
        String[] split = beanMethod.replace("?", "").split("\\.");
        if (split.length != 2) {
            throw new RuntimeException("表达式错误:" + beanMethod);
        }
        String beanName = split[0];
        String methodName = split[1];
        return (d) -> {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setBeanResolver(beanFactoryResolver);
            context.setVariable("data", d);
            Object ret = parser.parseExpression("@" + beanName + "." + methodName + "(#data)").getValue(context);
            if (isPeek) {
                return (R) d;
            }
            return (R) ret;
        };
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> invokeObjMethod(String exp) {
        boolean isPeek = Objects.equals(exp.charAt(0), '?');
        String methodName = exp.replaceAll("^(\\??\\[)([\\w$]+)(])", "$2");
        return d -> {
            R ret = (R) parser.parseExpression(methodName + "()").getValue(d);
            if (isPeek) return (R) d;
            return ret;
        };
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> invokeObjArgsMethod(String exp) {
        boolean isPeek = Objects.equals(exp.charAt(0), '?');
        String methodName = exp.replaceAll("^(\\??\\[)([a-zA-Z_$][\\S ]+)(])", "$2");
        return d -> {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setBeanResolver(beanFactoryResolver);
            context.setVariable("this", d);
            Object ret = parser.parseExpression(methodName).getValue(context, d);
            if (isPeek) {
                return (R) d;
            }
            return (R) ret;
        };
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> invokeStaticArgsMethod(String exp, Map<String, Class<?>> classMap) {
        boolean isPeek = Objects.equals(exp.charAt(0), '?');
        String clazzName = exp.replaceAll("^(\\??\\[\\^)([a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*\\.)([a-zA-Z_$][\\S ]+)]$", "$2");
        String methodName = exp.replaceAll("^(\\??\\[\\^[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*\\.)([a-zA-Z_$][\\S ]+)(])$", "$3");
        String staticClass = findStaticClass(clazzName, classMap).getName();
        return d -> {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("data", d);
            context.setBeanResolver(beanFactoryResolver);
            Object ret = parser.parseExpression("T(" + staticClass + ")." + methodName).getValue(context);
            if (isPeek) {
                return (R) d;
            }
            return (R) ret;
        };
    }

    private Class<?> findStaticClass(String clazzName, Map<String, Class<?>> classMap) {
        Map<String, Class<?>> map = new ConcurrentHashMap<>(classMap);
        map.putAll(defaultClassMap);
        if (clazzName.charAt(clazzName.length() - 1) == '.') {
            clazzName = clazzName.substring(0, clazzName.length() - 1);
        }
        Class<?> clazz = map.get(clazzName);
        try {
            return clazz != null ? clazz : Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            throw new ExpressionException("找不到类型:" + clazzName);
        }
    }

    @SuppressWarnings("unchecked")
    public <D, R> Function<D, R> invokeStaticMethod(String exp, Map<String, Class<?>> classMap) {
        boolean isPeek = Objects.equals(exp.charAt(0), '?');
        String clazzName = exp.replaceAll("^(\\??\\[\\^)([a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*\\.)([a-zA-Z_$][\\w$]+)]$", "$2");
        String methodName = exp.replaceAll("^(\\??\\[\\^[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*\\.)([a-zA-Z_$][\\w$]+)(])$", "$3");
        String staticClass = findStaticClass(clazzName, classMap).getName();
        return d -> {
            Object ret = parser.parseExpression("T(" + staticClass + ")." + methodName + "()").getValue();
            if (isPeek) {
                return (R) d;
            }
            return (R) ret;
        };
    }

    public <O, T> Demand<O, T> move(String od, String td) {
        return Demands.move(od, td);
    }

    public <O, T> Demand<O, T> exclude(String field) {
        return Demands.excludeAuto(field);
    }
}
