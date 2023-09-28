package xyz.fmcy.server.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;
import javassist.ClassPool;
import javassist.CtPrimitiveType;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.SignatureAttribute;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.database.QueryConfigure;
import xyz.fmcy.server.database.QuerySeed;
import xyz.fmcy.server.spring.annotation.AddGenerator;
import xyz.fmcy.server.spring.annotation.AnnotationProxy;
import xyz.fmcy.server.spring.annotation.FindPage;
import xyz.fmcy.server.standard.Result;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

@Demo(value = false)
class FmcySpringBootServerStanderdApplicationTests {
    String regex = "\\{(\\S+)}";
    ClassPool classPool = ClassPool.getDefault();

    @Test
    void contextLoads() throws NoSuchMethodException, JsonProcessingException, BadBytecode, InvocationTargetException, IllegalAccessException {
        Demo proxy = AnnotationProxy.proxy(FmcySpringBootServerStanderdApplicationTests.class.getAnnotation(Demo.class)).get();
        Demo proxy2 = AnnotationProxy.proxy(FmcySpringBootServerStanderdApplicationTests.class.getAnnotation(Demo.class)).get();
        System.out.println(proxy.equals(proxy2));
    }

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Demo {
    boolean value() default true;
}

