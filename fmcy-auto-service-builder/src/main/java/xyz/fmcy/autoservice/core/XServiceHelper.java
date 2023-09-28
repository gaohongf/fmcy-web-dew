package xyz.fmcy.autoservice.core;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.javassist.*;
import org.apache.ibatis.javassist.bytecode.SignatureAttribute;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import xyz.fmcy.autoservice.core.ssclass.SClassUtil;
import xyz.fmcy.mybatisplus.util.service.XServiceImpl;
import xyz.fmcy.server.database.XService;
import xyz.fmcy.util.entity.DemandHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@Configuration
public class XServiceHelper {
    private final GenericApplicationContext applicationContext;
    private final ClassPool pool = ClassPool.getDefault();

    public XServiceHelper(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public <O, T> void proxy(Class<O> sourceClazz, Class<T> targetClazz, Class<? extends XService<T>> superInterface, String name) throws Exception {
        String beanName = name != null && !name.isBlank() && !name.isEmpty() ? name : (Optional.of(sourceClazz.getSimpleName())
                .map(clazzName -> clazzName
                        .substring(0, 1)
                        .toLowerCase() +
                        clazzName.substring(1)).orElseThrow(RuntimeException::new) + "Service");
        Object service;
        if (superInterface.equals(XService.class)) {
            service = simpleEntityMapperService(sourceClazz, targetClazz).getConstructor().newInstance();
            applicationContext.registerBean(beanName, XService.class, () -> (XService<T>) service);
        } else {
            Class<?> hasParentService;
            if (superInterface.isInterface()) {
                System.out.println(1);
                hasParentService = createHasInterfaceParentService(sourceClazz, targetClazz, superInterface);
            } else {
                hasParentService = createHasClassParentService(sourceClazz, targetClazz, superInterface);
            }
            service = hasParentService.getConstructor().newInstance();
            GenericApplicationContext.class.getMethod("registerBean", String.class, Class.class, Supplier.class, BeanDefinitionCustomizer[].class)
                    .invoke(applicationContext,
                            beanName,
                            superInterface,
                            ((Supplier<XService<T>>) () -> superInterface.cast(service)),
                            new BeanDefinitionCustomizer[0]
                    );
        }
    }

    private <O, T> Class<?> createHasInterfaceParentService(Class<O> sourceClazz, Class<T> targetClazz, Class<? extends XService<T>> superInterface) throws CannotCompileException, NotFoundException {
        CtClass serviceCt = pool.makeClass(sourceClazz.getPackageName() + ".service." + sourceClazz.getSimpleName() + "ServiceImpl");
        serviceCt.setSuperclass(pool.makeClass(XServiceImpl.class.getName()));
        serviceCt.addInterface(pool.makeClass(superInterface.getName()));
        SignatureAttribute.ClassSignature signature = new SignatureAttribute.ClassSignature(null, new SignatureAttribute.ClassType(XServiceImpl.class.getName(), new SignatureAttribute.TypeArgument[]{
                new SignatureAttribute.TypeArgument(
                        new SignatureAttribute.ClassType(BaseMapper.class.getName(), new SignatureAttribute.TypeArgument[]{
                                SClassUtil.getTypeArgument(sourceClazz)
                        })
                ),
                SClassUtil.getTypeArgument(sourceClazz),
                SClassUtil.getTypeArgument(targetClazz)
        }), new SignatureAttribute.ClassType[]{
                new SignatureAttribute.ClassType(superInterface.getName(),
                        new SignatureAttribute.TypeArgument[]{
                                SClassUtil.getTypeArgument(sourceClazz)
                        })
        });
        Method[] methods = superInterface.getMethods();
        System.out.println(Arrays.toString(methods));
        serviceCt.setGenericSignature(signature.encode());
        setDemandHandler(serviceCt, sourceClazz, targetClazz);
        CtConstructor ctor = new CtConstructor(new CtClass[]{}, serviceCt);
        ctor.setBody("{}");
        serviceCt.addConstructor(ctor);
        return serviceCt.toClass();
    }

    private <O, T> Class<?> createHasClassParentService(Class<O> sourceClazz, Class<T> targetClazz, Class<? extends XService<T>> superClass) throws CannotCompileException, NotFoundException {
        CtClass serviceCt = pool.makeClass(sourceClazz.getPackageName() + ".service." + sourceClazz.getSimpleName() + "ServiceImpl");
        String superClassName = superClass.getName();
        serviceCt.setSuperclass(pool.makeClass(superClassName));
        setDemandHandler(serviceCt, sourceClazz, targetClazz);
        CtConstructor ctor = new CtConstructor(new CtClass[]{}, serviceCt);
        ctor.setBody("{}");
        serviceCt.addConstructor(ctor);
        return serviceCt.toClass();
    }

    private <O, T> Class<?> simpleEntityMapperService(Class<O> sourceClazz, Class<T> targetClazz) throws CannotCompileException, NotFoundException {
        CtClass serviceCt = pool.makeClass(sourceClazz.getPackageName() + ".service." + sourceClazz.getSimpleName() + "Service");
        serviceCt.setSuperclass(pool.makeClass(XServiceImpl.class.getName()));
        SignatureAttribute.ClassSignature signature = new SignatureAttribute.ClassSignature(null,
                new SignatureAttribute.ClassType(XServiceImpl.class.getName(), new SignatureAttribute.TypeArgument[]{
                        new SignatureAttribute.TypeArgument(
                                new SignatureAttribute.ClassType(BaseMapper.class.getName(), new SignatureAttribute.TypeArgument[]{
                                        SClassUtil.getTypeArgument(sourceClazz)
                                })
                        ),
                        SClassUtil.getTypeArgument(sourceClazz),
                        SClassUtil.getTypeArgument(targetClazz)
                }), null
        );
        serviceCt.setGenericSignature(signature.encode());
        setDemandHandler(serviceCt, sourceClazz, targetClazz);
        CtConstructor ctor = new CtConstructor(new CtClass[]{}, serviceCt);
        ctor.setBody("{}");
        serviceCt.addConstructor(ctor);
        return serviceCt.toClass();
    }

    private <O, T> void setDemandHandler(CtClass serviceCrClass, Class<O> sourceClazz, Class<T> targetClazz) throws NotFoundException, CannotCompileException {
        CtClass demandHandler = pool.get(DemandHandler.class.getName());
        serviceCrClass.addField(new CtField(demandHandler, "demandHandlerPToD", serviceCrClass));
        serviceCrClass.addField(new CtField(demandHandler, "demandHandlerDToP", serviceCrClass));
        CtMethod getDemandHandlerPToD = new CtMethod(demandHandler, "getDemandHandlerPToD", new CtClass[0], serviceCrClass);
        getDemandHandlerPToD.setBody("{return this.demandHandlerPToD != null?this.demandHandlerPToD:(this.demandHandlerPToD=xyz.fmcy.entity.E.mapper(" + sourceClazz.getName() + ".class, " + targetClazz.getName() + ".class));}");
        CtMethod getDemandHandlerDToP = new CtMethod(demandHandler, "getDemandHandlerDToP", new CtClass[0], serviceCrClass);
        getDemandHandlerDToP.setBody("{return this.demandHandlerDToP != null?this.demandHandlerDToP:(this.demandHandlerDToP=xyz.fmcy.entity.E.mapper(" + targetClazz.getName() + ".class, " + sourceClazz.getName() + ".class));}");
        serviceCrClass.addMethod(getDemandHandlerDToP);
        serviceCrClass.addMethod(getDemandHandlerPToD);
    }
}
