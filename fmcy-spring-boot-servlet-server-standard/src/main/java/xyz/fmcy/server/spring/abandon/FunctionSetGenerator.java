package xyz.fmcy.server.spring.abandon;

import javassist.*;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.fmcy.entity.E;
import xyz.fmcy.server.database.PageSeed;
import xyz.fmcy.server.database.QueryAttribute;
import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.database.QuerySeed;
import xyz.fmcy.server.spring.annotation.*;
import xyz.fmcy.server.spring.core.*;
import xyz.fmcy.server.standard.Result;
import xyz.fmcy.util.entity.Demands;
import xyz.fmcy.util.entity.EntityMapper;
import xyz.fmcy.util.entity.GroupEntityMapper;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FunctionSetGenerator {
    private static final List<Class<?>> QUERY_PROXY_CACHE = new Vector<>();
    public final static Map<Class<? extends java.lang.annotation.Annotation>, AnnotationToMethod> ANNOTATION_NEW_METHOD_INFO_MAP = new ConcurrentHashMap<>();

    static {
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(FindById.class, (clazz, declaring, annotation) -> {
                FindById findById = (FindById) annotation;
                return new HashMap<>(Map.of(FindById.class, new NewMethodInfo(declaring, "findById", Result.class,
                        List.of(new MethodParameterInfo(findById.idClass())), """
                        {
                            return findById((java.io.Serializable) $1, %s.class);
                        }
                        """.formatted(findById.viewType().getName()))
                        .addMethodAnnotationsAttribute(findById.mapping())
                        .addMethodAnnotationsAttribute(findById.otherMethodAnnotations())
                        .addParameterAnnotationsAttribute(findById.otherParamAnnotations())
                        .addParameterAnnotationsAttribute(new Annotation[][]{{findById.pathVariable()}})));
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(FindList.class, (clazz, declaring, annotation) -> {
                FindList findList = (FindList) annotation;
                return new HashMap<>(Map.of(FindList.class, new NewMethodInfo(declaring, "findList", Result.class,
                        List.of(new MethodParameterInfo(QuerySeed.class, List.of(findList.query()))), """
                        {
                            return findList($1, %s.class);
                        }
                        """.formatted(findList.viewType().getName()))
                        .addMethodAnnotationsAttribute(findList.mapping())
                        .addMethodAnnotationsAttribute(findList.otherMethodAnnotations())
                        .addParameterAnnotationsAttribute(findList.otherParamAnnotations())
                        .addParameterAnnotationsAttribute(new Class[][]{{RequestBody.class}})));
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(FindPage.class, (clazz, declaring, annotation) -> {
                FindPage findList = (FindPage) annotation;
                return new HashMap<>(Map.of(FindPage.class, new NewMethodInfo(declaring, "findPage", Result.class,
                        List.of(new MethodParameterInfo(PageSeed.class, List.of(findList.query()))), """
                        {
                            return findPage($1, %s.class);
                        }
                        """.formatted(findList.viewType().getName()))
                        .addMethodAnnotationsAttribute(findList.mapping())
                        .addMethodAnnotationsAttribute(findList.otherMethodAnnotations())
                        .addParameterAnnotationsAttribute(findList.otherParamAnnotations())
                        .addParameterAnnotationsAttribute(new Class[][]{{RequestBody.class}})));
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(FindGenerator.class, (clazz, declaring, annotation) -> {
                Map<Class<? extends Annotation>, NewMethodInfo> map = new HashMap<>();
                FindGenerator generator = (FindGenerator) annotation;
                Class<? extends QueryConfiguration> query = generator.query();
                boolean setAllQueryProxy = QueryProxy.class.equals(query);
                if (setAllQueryProxy) {
                    buildQueryProxy(clazz);
                }
                {
                    FindGenerator.EnableFindById enabled = generator.enableFindById();
                    if (enabled.value()) {
                        FindById findById = enabled.annotation();
                        map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(FindById.class).build(clazz, declaring, findById));
                    }
                }
                {
                    FindGenerator.EnableList enabled = generator.enableList();
                    if (enabled.value()) {
                        FindList findList = enabled.annotation();
                        map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(FindList.class).build(clazz, declaring, (
                                QueryProxy.class.equals(findList.query()) && !setAllQueryProxy
                        ) ? AnnotationProxy.proxy(findList).modify("query", query).get() : findList));
                    }
                }
                {
                    FindGenerator.EnablePage enabled = generator.enablePage();
                    if (enabled.value()) {
                        FindPage findPage = enabled.annotation();
                        map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(FindPage.class).build(clazz, declaring, (
                                QueryProxy.class.equals(findPage.query()) && !setAllQueryProxy
                        ) ? AnnotationProxy.proxy(findPage).modify("query", query).get() : findPage));
                    }
                }
                return map;
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(AddOne.class, (clazz, declaring, annotation) -> {
                AddOne addOne = (AddOne) annotation;
                return new HashMap<>(Map.of(AddOne.class, new NewMethodInfo(declaring, "addOne", Result.class,
                        List.of(new MethodParameterInfo(addOne.insertClass())), """
                        {
                            return super.addOne((java.io.Serializable) $1);
                        }
                        """)
                        .addMethodAnnotationsAttribute(addOne.mapping())
                        .addMethodAnnotationsAttribute(addOne.otherMethodAnnotations())
                        .addParameterAnnotationsAttribute(addOne.otherParamAnnotations())
                        .addParameterAnnotationsAttribute(new Class[][]{{RequestBody.class}})));
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(AddList.class, (clazz, declaring, annotation) -> {
                AddList addList = (AddList) annotation;
                return new HashMap<>(Map.of(AddList.class, new NewMethodInfo(declaring, "addList", Result.class,
                        List.of(new MethodParameterInfo(List.class, List.of(addList.insertClass()))), """
                        {
                            return super.addList((java.util.List) $1);
                        }
                        """)
                        .addMethodAnnotationsAttribute(addList.mapping())
                        .addMethodAnnotationsAttribute(addList.otherMethodAnnotations())
                        .addParameterAnnotationsAttribute(addList.otherParamAnnotations())
                        .addParameterAnnotationsAttribute(new Class[][]{{RequestBody.class}})));
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(AddGenerator.class, (clazz, declaring, annotation) -> {
                Map<Class<? extends Annotation>, NewMethodInfo> map = new HashMap<>();
                AddGenerator generator = (AddGenerator) annotation;
                Class<? extends Serializable> insertClass = generator.insertClass();
                boolean setInsertClass = !Serializable.class.equals(insertClass);
                {
                    AddGenerator.EnableAddList enabled = generator.enableList();
                    if (enabled.value()) {
                        AddList addList = enabled.annotation();
                        map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(AddList.class).build(clazz, declaring,
                                        Serializable.class.equals(addList.insertClass()) && setInsertClass
                                                ? AnnotationProxy.proxy(addList).modify("insertClass", insertClass).get()
                                                : addList
                                )
                        );
                    }
                }
                {
                    AddGenerator.EnableAddOne enabled = generator.enableOne();
                    if (enabled.value()) {
                        AddOne addOne = enabled.annotation();
                        map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(AddOne.class).build(clazz, declaring,
                                        Serializable.class.equals(addOne.insertClass()) && setInsertClass
                                                ? AnnotationProxy.proxy(addOne).modify("insertClass", insertClass).get()
                                                : addOne
                                )
                        );
                    }
                }
                return new HashMap<>(map);
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(UpdateById.class, (clazz, declaring, annotation) -> {
                UpdateById updateById = (UpdateById) annotation;
                return new HashMap<>(Map.of(UpdateById.class, new NewMethodInfo(declaring, "updateById", Result.class,
                        List.of(new MethodParameterInfo(updateById.updaterClass())), """
                        {
                            return super.updateById((java.io.Serializable) $1);
                        }
                        """)
                        .addMethodAnnotationsAttribute(updateById.mapping())
                        .addMethodAnnotationsAttribute(updateById.otherMethodAnnotations())
                        .addParameterAnnotationsAttribute(updateById.otherParamAnnotations())
                        .addParameterAnnotationsAttribute(new Class[][]{{RequestBody.class}})));
            });
        }
        {
            ANNOTATION_NEW_METHOD_INFO_MAP.put(Abandon.class, (clazz, declaring, annotation) -> {
                Map<Class<? extends Annotation>, NewMethodInfo> map = new HashMap<>();
                map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(AddGenerator.class).build(clazz, declaring, annotation));
                map.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(FindGenerator.class).build(clazz, declaring, annotation));
                return map;
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Annotation> void readGeneratorChild(
            Annotation annotation,
            Consumer<E> annotationConsumer
    ) {
        try {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            if ((boolean) annotationClass.getMethod("value").invoke(annotation)) {
                E childAnnotation = (E) annotationClass.getMethod("annotation").invoke(annotation);
                Optional.ofNullable(annotationConsumer).ifPresent(consumer -> consumer.accept(childAnnotation));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Class<? extends QueryConfiguration> buildQueryProxy(Class<?> clazz) {
        if (QUERY_PROXY_CACHE.contains(clazz)) return QueryProxy.class;
        EntityMapper<QueryProxy, ?> mapper = EntityMapper.getMapper(QueryProxy.class, clazz);
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().length() > 3)
                .filter(method -> method.getName().startsWith("set"))
                .forEach(method -> {
                    String substring = method.getName().substring(3);
                    String fieldName = ((char) (substring.charAt(0) + 32)) + substring.substring(1);
                    Class<?> parameterType = method.getParameterTypes()[0];
                    mapper.want(Demands.mapsMove(proxy -> proxy.get(fieldName), $ -> $.map(QueryAttribute::getValue)
                                    .filter(data -> !"null".equals(data))
                                    .map(data -> BaseDataTypeCast.cast(data, parameterType)),
                            (result, data) -> {
                                method.setAccessible(true);
                                try {
                                    method.invoke(result, data);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }));
                });
        E.addMapper(GroupEntityMapper.DEF_GROUP, mapper);
        QUERY_PROXY_CACHE.add(clazz);
        return QueryProxy.class;
    }

}
