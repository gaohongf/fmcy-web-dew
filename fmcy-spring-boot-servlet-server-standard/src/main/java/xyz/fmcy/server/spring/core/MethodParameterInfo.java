package xyz.fmcy.server.spring.core;

import javassist.bytecode.SignatureAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodParameterInfo {
    private final Class<?> parameterType;
    private final List<Class<?>> methodSignatures;

    public MethodParameterInfo(Class<?> parameterType, List<Class<?>> methodSignatures) {
        this.parameterType = parameterType;
        this.methodSignatures = new ArrayList<>(Objects.requireNonNullElseGet(methodSignatures, ArrayList::new));
    }

    public MethodParameterInfo(Class<?> parameterType) {
        this(parameterType, null);
    }

    public SignatureAttribute.ClassType getSignatures() {
        SignatureAttribute.TypeArgument[] arguments = methodSignatures.stream().map(
                parameter -> new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(parameter.getName()))
        ).toArray(SignatureAttribute.TypeArgument[]::new);
        return new SignatureAttribute.ClassType(parameterType.getName(), arguments.length != 0 ? arguments : null);
    }

    public String encodeSignatures() {
        return getSignatures().encode();
    }

    public Class<?> getParameterType() {
        return parameterType;
    }
}
