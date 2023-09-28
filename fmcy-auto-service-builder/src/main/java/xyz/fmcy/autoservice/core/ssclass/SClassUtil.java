package xyz.fmcy.autoservice.core.ssclass;

import org.apache.ibatis.javassist.bytecode.SignatureAttribute;

import java.util.List;

public final class SClassUtil {

    public static SignatureAttribute.ClassSignature getClassSignature(SignatureAttribute.TypeParameter[] params,
                                                                      SignatureAttribute.ClassType superClass,
                                                                      SignatureAttribute.ClassType[] interfaces) {
        return new SignatureAttribute.ClassSignature(params, superClass, interfaces);
    }

    public static SignatureAttribute.ClassType getClassType(Class<?> type, List<Class<?>> classes) {
        return new SignatureAttribute.ClassType(type.getName(),
                classes.stream().map(SClassUtil::getTypeArgument).toArray(SignatureAttribute.TypeArgument[]::new)
        );
    }

    public static SignatureAttribute.TypeArgument getTypeArgument(Class<?> type) {
        return new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType((type.getName())));
    }

    public static SignatureAttribute.TypeParameter getTypeParameter(Class<?> type) {
        return new SignatureAttribute.TypeParameter(type.getName());
    }

}
