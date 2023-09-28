package xyz.fmcy.server.spring.core;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.SignatureAttribute;

import java.util.List;

public class CtMethodParameterInfo {
    private final ClassPool classPool;
    private final List<MethodParameterInfo> methodParameterInfos;

    public CtMethodParameterInfo(ClassPool classPool, List<MethodParameterInfo> methodParameterInfos) {
        this.classPool = classPool;
        this.methodParameterInfos = methodParameterInfos;
    }

    public SignatureAttribute.Type[] encodeSignatures() {
        return methodParameterInfos.stream().map(MethodParameterInfo::getSignatures).toArray(SignatureAttribute.Type[]::new);
    }

    public CtClass[] getParameters() {
        return methodParameterInfos.stream().map(MethodParameterInfo::getParameterType)
                .map(Class::getName)
                .map(classPool::makeClass)
                .toArray(CtClass[]::new);
    }

}
