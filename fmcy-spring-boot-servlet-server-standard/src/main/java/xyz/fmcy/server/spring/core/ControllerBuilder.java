package xyz.fmcy.server.spring.core;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import xyz.fmcy.server.spring.BaseRestController;
import xyz.fmcy.server.spring.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static xyz.fmcy.server.spring.abandon.FunctionSetGenerator.ANNOTATION_NEW_METHOD_INFO_MAP;

@Configuration
public class ControllerBuilder {
    private final GenericApplicationContext applicationContext;
    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public ControllerBuilder(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 控制加载顺序
     */
    public final static List<Class<? extends java.lang.annotation.Annotation>> LOAD_METHOD_ORDER = new Vector<>();

    static {
        LOAD_METHOD_ORDER.addAll(List.of(
                Abandon.class,
                AddGenerator.class,
                QueryGenerator.class,
                AddOne.class,
                AddList.class,
                FindById.class,
                FindList.class,
                FindPage.class,
                UpdateById.class,
                DeleteById.class
        ));
    }

    @SuppressWarnings("all")
    @PostConstruct
    public void loadController() {
        Map<String, BaseRestController> beans =
                applicationContext.getBeansOfType(BaseRestController.class);
        beans.forEach((name, bean) -> {
            try {
                Class<? extends BaseRestController<?>> build = build(bean);
                Object bean1 = applicationContext.getBean(name);
                applicationContext.registerBean(name, build);
                removeMapping(bean1.getClass());
                addMapping(name);
            } catch (CannotCompileException | NotFoundException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void removeMapping(Class<?> beanClass) {
        ReflectionUtils.doWithMethods(beanClass, method -> {
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, beanClass);
            try {
                Method createMappingMethod = RequestMappingHandlerMapping.class.getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                createMappingMethod.setAccessible(true);
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo) createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, beanClass);
                if (requestMappingInfo != null) {
                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
    }

    public void addMapping(String beanName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
        method.setAccessible(true);
        method.invoke(requestMappingHandlerMapping, beanName);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends BaseRestController<?>> build(BaseRestController<?> controller) throws CannotCompileException, NotFoundException {
        Class<? extends BaseRestController<?>> clazz = (Class<? extends BaseRestController<?>>) controller.getClass();
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(clazz.getName());
        ctClass.setName(clazz.getPackageName() + ".$proxy." + clazz.getSimpleName());
        Map<Class<? extends Annotation>, NewMethodInfo> methodInfoMap = new HashMap<>();
        LOAD_METHOD_ORDER.forEach((key) -> {
            Annotation annotation = clazz.getAnnotation(key);
            if (annotation != null) {
                methodInfoMap.putAll(ANNOTATION_NEW_METHOD_INFO_MAP.get(key).build(controller.getService().resultclass(), ctClass, annotation));
            }
        });
        methodInfoMap.values().forEach(methodInfo -> {
            try {
                ClassBuilderUtil.createCtMethod(methodInfo);
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
        return (Class<? extends BaseRestController<?>>) ctClass.toClass();
    }
}
