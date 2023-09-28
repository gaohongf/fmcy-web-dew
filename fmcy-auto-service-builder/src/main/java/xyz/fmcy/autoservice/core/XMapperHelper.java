package xyz.fmcy.autoservice.core;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.bytecode.SignatureAttribute;
import org.apache.ibatis.session.SqlSessionFactory;

import org.mybatis.spring.mapper.MapperFactoryBean;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import xyz.fmcy.autoservice.core.ssclass.SClassUtil;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Configuration
public class XMapperHelper {
    private final GenericApplicationContext applicationContext;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public XMapperHelper(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <O> void createMapper(Class<O> sourceClazz, String name) throws Exception {
        String packageName = sourceClazz.getPackageName();
        String simpleName = sourceClazz.getSimpleName();
        String mapperClassName = packageName + ".mapper." + simpleName + "Mapper";
        String beanName = name != null && !name.isBlank() && !name.isEmpty() ? name : (Optional.of(simpleName)
                .map(clazzName -> clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1))
                .orElseThrow(RuntimeException::new) + "Mapper");
        ClassPool classPool = ClassPool.getDefault();
        CtClass baseMapper = classPool.get(BaseMapper.class.getName());
        CtClass mapperCt = classPool.makeInterface(mapperClassName, baseMapper);
        mapperCt.setGenericSignature(
                SClassUtil.getClassSignature(null, null, new SignatureAttribute.ClassType[]{
                        SClassUtil.getClassType(BaseMapper.class, List.of(sourceClazz))}
                ).encode()
        );
        Class<?> mapperClass = mapperCt.toClass();
        MapperFactoryBean<?> factoryBean = new MapperFactoryBean<>(mapperClass);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        sqlSessionFactory.getConfiguration().addMapper(mapperClass);
        Method checkDaoConfig = MapperFactoryBean.class.getDeclaredMethod("checkDaoConfig");
        checkDaoConfig.setAccessible(true);
        checkDaoConfig.invoke(factoryBean);
        applicationContext.registerBean(beanName, MapperFactoryBean.class, () -> factoryBean);
    }
}
