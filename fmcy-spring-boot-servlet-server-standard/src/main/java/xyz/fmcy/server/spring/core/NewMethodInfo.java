package xyz.fmcy.server.spring.core;

import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class NewMethodInfo {
    private final CtClass declaring;
    private final String name;
    private final Class<?> returnType;
    private final List<MethodParameterInfo> parameterInfos;
    private final String methodBody;
    private final ConstPool constPool;
    private AnnotationsAttribute methodAnnotationsAttribute;
    private ParameterAnnotationsAttribute parameterAnnotationsAttribute;

    public NewMethodInfo(CtClass declaring, String name, Class<?> returnType, List<MethodParameterInfo> parameterInfos, String methodBody) {
        this.declaring = declaring;
        this.name = name;
        this.returnType = returnType;
        this.parameterInfos = new ArrayList<>(Objects.requireNonNullElseGet(parameterInfos, ArrayList::new));
        this.methodBody = methodBody;
        this.constPool = declaring.getClassFile().getConstPool();
    }

    @Override
    public String toString() {
        return "NewMethodInfo{" +
                "declaring=" + declaring +
                ", name='" + name + '\'' +
                ", returnType=" + returnType +
                ", parameterInfos=" + parameterInfos +
                ", methodBody='" + methodBody + '\'' +
                ", constPool=" + constPool +
                ", methodAnnotationsAttribute=" + methodAnnotationsAttribute +
                ", parameterAnnotationsAttribute=" + parameterAnnotationsAttribute +
                '}';
    }

    public NewMethodInfo addMethodAnnotationsAttribute(java.lang.annotation.Annotation... annotation) {
        return addMethodAnnotationsAttribute(ClassBuilderUtil.createMethodAnnotationsAttribute(constPool, annotation));
    }

    public NewMethodInfo addMethodAnnotationsAttribute(Annotations... annotations) {
        AnnotationsAttribute attribute = ClassBuilderUtil.createOuterMethodAnnotationsAttribute(constPool, annotations);
        return addMethodAnnotationsAttribute(attribute);
    }

    @SuppressWarnings("unchecked")
    public NewMethodInfo addMethodAnnotationsAttribute(Class<java.lang.annotation.Annotation>... annotations) {
        return addMethodAnnotationsAttribute(Arrays.stream(annotations)
                .map(clazz -> new Annotation(clazz.getName(), constPool)).toArray(Annotation[]::new)
        );
    }

    public NewMethodInfo addMethodAnnotationsAttribute(Annotation... annotation) {
        AnnotationsAttribute attribute = ClassBuilderUtil.createMethodAnnotationsAttribute(constPool, annotation);
        return addMethodAnnotationsAttribute(attribute);
    }

    public NewMethodInfo addMethodAnnotationsAttribute(AnnotationsAttribute attribute) {
        if (methodAnnotationsAttribute != null) {
            ClassBuilderUtil.appendMethodAnnotationsAttribute(methodAnnotationsAttribute, attribute);
        } else {
            this.methodAnnotationsAttribute = attribute;
        }
        return this;
    }

    public NewMethodInfo addParameterAnnotationsAttribute(java.lang.annotation.Annotation[]... annotation) {
        return addParameterAnnotationsAttribute(ClassBuilderUtil.createParameterAnnotationsAttribute(constPool, annotation));
    }

    @SuppressWarnings("unchecked")
    public NewMethodInfo addParameterAnnotationsAttribute(Class<? extends java.lang.annotation.Annotation>[]... annotation) {
        return addParameterAnnotationsAttribute(Arrays.stream(annotation).map(an -> Arrays.stream(an)
                .map(clazz -> new Annotation(clazz.getName(), constPool))
                .toArray(Annotation[]::new)).toArray(Annotation[][]::new)
        );
    }

    public NewMethodInfo addParameterAnnotationsAttribute(ParamAnnotations... annotation) {
        return addParameterAnnotationsAttribute(ClassBuilderUtil.createOuterParameterAnnotationsAttribute(constPool, annotation));
    }

    public NewMethodInfo addParameterAnnotationsAttribute(Annotation[]... annotations) {
        ParameterAnnotationsAttribute attribute = ClassBuilderUtil.createParameterAnnotationsAttribute(constPool, annotations);
        return addParameterAnnotationsAttribute(attribute);
    }

    public NewMethodInfo addParameterAnnotationsAttribute(ParameterAnnotationsAttribute attribute) {
        if (parameterAnnotationsAttribute != null) {
            ClassBuilderUtil.appendParamAnnotationsAttribute(parameterAnnotationsAttribute, attribute);
        } else {
            this.parameterAnnotationsAttribute = attribute;
        }
        return this;
    }

    public CtClass getDeclaring() {
        return declaring;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public List<MethodParameterInfo> getParameterTypes() {
        return parameterInfos;
    }

    public String getMethodBody() {
        return methodBody;
    }

    public AnnotationsAttribute getMethodAnnotationsAttribute() {
        return methodAnnotationsAttribute;
    }

    public ParameterAnnotationsAttribute getParameterAnnotationsAttribute() {
        return parameterAnnotationsAttribute;
    }
}
