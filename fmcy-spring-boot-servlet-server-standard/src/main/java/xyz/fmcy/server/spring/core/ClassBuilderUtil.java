package xyz.fmcy.server.spring.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.*;
import xyz.fmcy.server.standard.Result;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ClassBuilderUtil {
    public static ClassPool pool = ClassPool.getDefault();
    public final static Map<Class<?>, Map.Entry<Class<?>, CtClass>> classMap = Map.ofEntries(
            Map.entry(boolean.class, Map.entry(boolean.class, CtClass.booleanType)),
            Map.entry(byte.class, Map.entry(byte.class, CtClass.byteType)),
            Map.entry(char.class, Map.entry(char.class, CtClass.charType)),
            Map.entry(short.class, Map.entry(short.class, CtClass.shortType)),
            Map.entry(int.class, Map.entry(int.class, CtClass.intType)),
            Map.entry(long.class, Map.entry(long.class, CtClass.longType)),
            Map.entry(float.class, Map.entry(float.class, CtClass.floatType)),
            Map.entry(double.class, Map.entry(double.class, CtClass.doubleType)),
            Map.entry(Boolean.class, Map.entry(boolean.class, CtClass.booleanType)),
            Map.entry(Byte.class, Map.entry(byte.class, CtClass.byteType)),
            Map.entry(Character.class, Map.entry(char.class, CtClass.charType)),
            Map.entry(Short.class, Map.entry(short.class, CtClass.shortType)),
            Map.entry(Integer.class, Map.entry(int.class, CtClass.intType)),
            Map.entry(Long.class, Map.entry(long.class, CtClass.longType)),
            Map.entry(Float.class, Map.entry(float.class, CtClass.floatType)),
            Map.entry(Double.class, Map.entry(double.class, CtClass.doubleType))
    );


    static {
        pool.importPackage(Result.class.getPackageName());
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private final static List<String> excludeAnnotationMethod = Arrays.stream(java.lang.annotation.Annotation.class.getMethods())
            .map(Method::getName).toList();

    public static Annotation createAnnotation(java.lang.annotation.Annotation javaAnnotation, Class<? extends java.lang.annotation.Annotation> annotationType, ConstPool constPool) {
        Annotation annotation = new Annotation(annotationType.getName(), constPool);
        Arrays.stream(annotationType.getDeclaredMethods())
                .filter(Predicate.not(method -> excludeAnnotationMethod.contains(method.getName())))
                .forEach(method -> {
                    try {
                        Object invoke = method.invoke(javaAnnotation);
                        annotation.addMemberValue(method.getName(), createMemberValue(invoke.getClass(), invoke, constPool));
                    } catch (InvocationTargetException | IllegalAccessException | NotFoundException |
                             NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                });
        return annotation;
    }

    public static Annotation createSimpleAnnotation(Class<java.lang.annotation.Annotation> annotationClass, ConstPool constPool) {
        return new Annotation(annotationClass.getName(), constPool);
    }

    @SuppressWarnings("unchecked")
    public static MemberValue createMemberValue(Class<?> type, Object value, ConstPool constPool) throws NotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map.Entry<Class<?>, CtClass> classCtClassEntry = classMap.get(type);
        MemberValue memberValue = Annotation.createMemberValue(constPool, classCtClassEntry != null ? classCtClassEntry.getValue() : pool.get(type.getName()));
        if (memberValue instanceof ClassMemberValue classMemberValue) {
            classMemberValue.setValue(((Class<?>) value).getName());
        } else if (memberValue instanceof EnumMemberValue enumMemberValue) {
            enumMemberValue.setValue(((Enum<?>) value).name());
        } else if (memberValue instanceof ArrayMemberValue arrayMemberValue) {
            Object[] array = (Object[]) value;
            MemberValue[] values = new MemberValue[array.length];
            for (int i = 0; i < array.length; i++) {
                values[i] = createMemberValue(array[i].getClass(), array[i], constPool);
            }
            arrayMemberValue.setValue(values);
        } else if (memberValue instanceof AnnotationMemberValue annotationMemberValue) {
            annotationMemberValue.setValue(createAnnotation((java.lang.annotation.Annotation) value, (Class<? extends java.lang.annotation.Annotation>) type, constPool));
        } else {
            memberValue.getClass().getMethod("setValue", classCtClassEntry != null ? classCtClassEntry.getKey() : type).invoke(memberValue, value);
        }
        return memberValue;
    }

    public static AnnotationsAttribute createMethodAnnotationsAttribute(ConstPool constPool, java.lang.annotation.Annotation... annotations) {
        return createMethodAnnotationsAttribute(constPool, Arrays.stream(annotations)
                .map(annotation -> createAnnotation(annotation, annotation.annotationType(), constPool))
                .toArray(Annotation[]::new));
    }

    public static AnnotationsAttribute createMethodAnnotationsAttribute(ConstPool constPool, Annotation... annotations) {
        AnnotationsAttribute ma = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        ma.setAnnotations(annotations);
        return ma;
    }

    public static ParameterAnnotationsAttribute createParameterAnnotationsAttribute(ConstPool constPool, java.lang.annotation.Annotation[]... annotations) {
        return createParameterAnnotationsAttribute(constPool, Arrays.stream(annotations)
                .map(annotations1 -> Arrays.stream(annotations1)
                        .map(annotation -> createAnnotation(annotation, annotation.annotationType(), constPool))
                        .toArray(Annotation[]::new))
                .toArray(Annotation[][]::new));
    }

    public static ParameterAnnotationsAttribute createParameterAnnotationsAttribute(ConstPool constPool, Annotation[]... annotations) {
        ParameterAnnotationsAttribute pa = new ParameterAnnotationsAttribute(constPool, ParameterAnnotationsAttribute.visibleTag);
        pa.setAnnotations(annotations);
        return pa;
    }

    public static void createCtMethod(CtClass declaring, String name, CtClass returnType, CtMethodParameterInfo parameters, String body, AttributeInfo... attributes) throws CannotCompileException {
        CtMethod make = new CtMethod(returnType, name, parameters.getParameters(), declaring);
        MethodInfo methodInfo = make.getMethodInfo();
        Arrays.stream(attributes).forEach(methodInfo::addAttribute);
        try {
            var originalSignature = SignatureAttribute.toMethodSignature(make.getSignature());
            SignatureAttribute.MethodSignature methodSignature = new SignatureAttribute.MethodSignature(
                    originalSignature.getTypeParameters(),
                    parameters.encodeSignatures(),
                    originalSignature.getReturnType(),
                    originalSignature.getExceptionTypes()
            );
            make.setGenericSignature(methodSignature.encode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        make.setBody(body);
        declaring.addMethod(make);
    }

    public static Annotation analysisAnnotations(Annotations annotations, ConstPool constPool) {
        Class<? extends java.lang.annotation.Annotation> annotationClass = annotations.type();
        Annotation annotation = new Annotation(annotationClass.getName(), constPool);
        Arrays.stream(annotations.fields()).forEach(field -> {
            try {
                String name = field.name();
                Method method = annotationClass.getDeclaredMethod(name);
                String value = field.value();
                Class<?> returnType = method.getReturnType();
                annotation.addMemberValue(name, createMemberValueByStringValue(returnType, value, constPool));
            } catch (NoSuchMethodException | NotFoundException | InvocationTargetException | IllegalAccessException |
                     ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        return annotation;
    }

    @SuppressWarnings("unchecked")
    public static MemberValue createMemberValueByStringValue(Class<?> type, String value, ConstPool constPool) throws ClassNotFoundException, NotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        MemberValue memberValue;
        if (Class.class.equals(type)) {
            memberValue = createMemberValue(Class.class, Class.forName(value), constPool);
        } else if (type.isEnum()) {
            memberValue = createMemberValue(Enum.class, Enum.valueOf((Class) type, value), constPool);
        } else if (type.isArray()) {
            Class<?> arrayType = type.getComponentType();
            try {
                ArrayList<String> arrayList = jsonMapper.readValue(value, jsonMapper.getTypeFactory().constructCollectionType(
                        ArrayList.class, String.class
                ));
                ArrayList<MemberValue> memberValues = new ArrayList<>();
                for (String s : arrayList) {
                    memberValues.add(createMemberValueByStringValue(arrayType, s, constPool));
                }
                ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constPool);
                arrayMemberValue.setValue(memberValues.toArray(MemberValue[]::new));
                memberValue = arrayMemberValue;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else if (type.isAnnotation()) {
            try {
                AnnotationsAnnotation annotation = jsonMapper.readValue(value, AnnotationsAnnotation.class);
                AnnotationMemberValue annotationMemberValue = new AnnotationMemberValue(constPool);
                annotationMemberValue.setValue(analysisAnnotations(annotation, constPool));
                memberValue = annotationMemberValue;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (BaseDataTypeCast.catCast(value.getClass()) && BaseDataTypeCast.catCast(type)) {
                Object cast = BaseDataTypeCast.cast(value, type);
                memberValue = createMemberValue(type, cast, constPool);
            } else {
                throw new RuntimeException("无法解析值");
            }
        }
        return memberValue;
    }

    public static AnnotationsAttribute createOuterMethodAnnotationsAttribute(ConstPool constPool, Annotations... annotations) {
        Annotation[] otherMethodAnnotation = Arrays.stream(annotations)
                .map(annotations1 -> ClassBuilderUtil.analysisAnnotations(annotations1, constPool))
                .toArray(Annotation[]::new);
        return createMethodAnnotationsAttribute(constPool, otherMethodAnnotation);
    }

    public static Annotation[] analysisParameterAnnotations(ConstPool constPool, ParamAnnotations annotations) {
        return Arrays.stream(annotations.value()).map(annotation -> analysisAnnotations(annotation, constPool)).toArray(Annotation[]::new);
    }

    public static ParameterAnnotationsAttribute createOuterParameterAnnotationsAttribute(ConstPool constPool, ParamAnnotations... paramAnnotations) {
        return createParameterAnnotationsAttribute(constPool, Arrays.stream(paramAnnotations)
                .map(annotations -> analysisParameterAnnotations(constPool, annotations))
                .toArray(Annotation[][]::new)
        );
    }

    public static AnnotationsAttribute appendMethodAnnotationsAttribute(AnnotationsAttribute attribute1, AnnotationsAttribute attribute2) {
        Arrays.stream(attribute2.getAnnotations())
                .forEach(attribute1::addAnnotation);
        return attribute1;
    }

    public static AnnotationsAttribute appendMethodAnnotationsAttribute(AnnotationsAttribute attribute1, AnnotationsAttribute... attribute2) {
        Arrays.stream(attribute2).forEach(attribute -> appendMethodAnnotationsAttribute(attribute1, attribute));
        return attribute1;
    }


    public static ParameterAnnotationsAttribute appendParamAnnotationsAttribute(ParameterAnnotationsAttribute attribute1, ParameterAnnotationsAttribute attribute2) {
        Annotation[][] annotations1 = attribute1.getAnnotations();
        Annotation[][] annotations2 = attribute2.getAnnotations();
        Annotation[][] results = new Annotation[Math.max(annotations1.length, annotations2.length)][];
        for (int i = 0; i < results.length; i++) {
            List<Annotation> annotations = new ArrayList<>();
            if (annotations1.length > i) {
                annotations.addAll(Arrays.stream(annotations1[i]).toList());
            }
            if (annotations2.length > i) {
                annotations.addAll(Arrays.stream(annotations2[i]).toList());
            }
            results[i] = annotations.toArray(Annotation[]::new);
        }
        attribute1.setAnnotations(results);
        return attribute1;
    }

    public static ParameterAnnotationsAttribute appendParamAnnotationsAttribute(ParameterAnnotationsAttribute attribute1, ParameterAnnotationsAttribute... attribute2) {
        Arrays.stream(attribute2).forEach(attribute -> appendParamAnnotationsAttribute(attribute1, attribute));
        return attribute1;
    }

    public static void createCtMethod(NewMethodInfo methodInfo) throws CannotCompileException {
        createCtMethod(
                methodInfo.getDeclaring(),
                methodInfo.getName(),
                methodInfo.getReturnType(),
                methodInfo.getParameterTypes(),
                methodInfo.getMethodBody(),
                methodInfo.getMethodAnnotationsAttribute(),
                methodInfo.getParameterAnnotationsAttribute()
        );
    }

    public static void createCtMethod(CtClass declaring, String name, Class<?> returnType, List<MethodParameterInfo> parameterInfos, String methodBody,
                                      AnnotationsAttribute methodAnnotationsAttribute,
                                      ParameterAnnotationsAttribute parameterAnnotationsAttribute)
            throws CannotCompileException {
        createCtMethod(declaring, name, pool.makeClass(returnType.getName()), new CtMethodParameterInfo(pool, parameterInfos), methodBody, methodAnnotationsAttribute, parameterAnnotationsAttribute);
    }
}
