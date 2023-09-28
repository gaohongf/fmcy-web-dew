package demo.test;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static String code = """
            package xyz.fmcy;
                        
            class HelloWord {
                public void say() {
                    System.out.println("Hello Word");
                }
            }
            """;
    public static void main(String[] args) throws URISyntaxException {
        test1();
    }

    public static void test1() throws URISyntaxException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        MyJavaFileObject myJavaFileObject = new MyJavaFileObject("xyz.fmcy.HelloWord", code);
        Boolean call = javaCompiler.getTask(null, standardFileManager, null, null, null,
                List.of(myJavaFileObject)
        ).call();
//        System.out.println(Arrays.toString(myJavaFileObject.getByteCode()));
//        byte[] byteArray = outputStream.toByteArray();
    }
}

class MyJavaFileObject extends SimpleJavaFileObject{

    private ByteArrayOutputStream byteArrayOutputStream;
    private final CharSequence code;

    public MyJavaFileObject(String classname,String code) throws URISyntaxException {
        super(new URI(classname), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }

    @Override
    public OutputStream openOutputStream() {
        return byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public byte[] getByteCode(){
        return byteArrayOutputStream.toByteArray();
    }
}
