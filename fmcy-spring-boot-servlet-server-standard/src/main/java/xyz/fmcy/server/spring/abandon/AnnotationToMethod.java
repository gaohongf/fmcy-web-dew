package xyz.fmcy.server.spring.abandon;

import javassist.CtClass;
import xyz.fmcy.server.spring.core.NewMethodInfo;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 描述注解转为Method的方式
 */
@FunctionalInterface
public interface AnnotationToMethod {
    /**
     * 构建方法的信息
     *
     * @param serviceResultClass 与服务交互产生的结果类型
     * @param declaring          所在父类
     * @param annotation         注解
     * @return 创建的新方法信息集
     */
    Map<Class<? extends Annotation>, NewMethodInfo> build(
            Class<?> serviceResultClass,
            CtClass declaring,
            Annotation annotation
    );
}
