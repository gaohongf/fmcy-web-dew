package xyz.fmcy.server.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import org.junit.jupiter.api.Test;
import xyz.fmcy.server.spring.annotation.proxy.AnnotationProxy;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

class FmcySpringBootServerStanderdApplicationTests {
    String regex = "\\{(\\S+)}";
    ClassPool classPool = ClassPool.getDefault();

    @Test
    void contextLoads() throws NoSuchMethodException, JsonProcessingException, BadBytecode, InvocationTargetException, IllegalAccessException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(0,-1);
        list.add(3,5);
        System.out.println(list);
    }

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Demo {
    boolean value() default true;
}

