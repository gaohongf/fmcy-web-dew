package xyz.fmcy.util.auto;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 付高宏
 * @date 2023/1/31 10:14
 */
public abstract class PackageScanner {

    private final List<Predicate<Class<?>>> filter = new Vector<>();
    private final List<String> excludeClass = new Vector<>();
    private final List<String> excludePackage = new Vector<>();

    private PackageScanner() {
    }

    protected abstract void accept(Class<?> clazz);
    public PackageScanner excludeClass(String className){
        excludeClass.add(className);
        return this;
    }

    public PackageScanner excludePackage(String packageName){
        excludePackage.add(packageName);
        return this;
    }
    private void packetScanner(File curFile, String packName) {
        //如果不是目录就结束方法的调用
        if (!curFile.isDirectory()) {
            return;
        }
        //该方法返回一个抽象路径名数组，表示由该抽象路径名表示的目录中的文件
        File[] files = curFile.listFiles();
        for (int i = 0; files != null && files.length > i; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(".class")) {
                String fileName = files[i].getName().replace(".class", "");
                if (excludePackage.contains(packName)){
                    continue;
                }
                //去掉“.class”后就是文件名，路径名加文件名就是类名
                String className = packName + "." + fileName;
                if (excludeClass.contains(className)){
                    continue;
                }
                try {
                    //根据类名称得到类类型
                    Class<?> clazz = Class.forName(className);
                    accept(clazz);
                } catch (ClassNotFoundException e) {
                    System.out.println(className + ":warn/异常类型");
                }
            } else if (files[i].isDirectory()) {
                //如果该文件是目录就再一次调用此方法，将路径名加文件名（下一次路径）传过去
                //一直这样递归调用，直到是文件为止
                packetScanner(files[i], !"".endsWith(packName) ? packName + "." + files[i].getName() : files[i].getName());
            }
        }
    }

    private void scanJarPacket(URL url) throws IOException {
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarName = jarEntry.getName();
            //如果它是一个目录或者不是“.class”文件，就跳过
            if (jarEntry.isDirectory() || !jarName.endsWith(".class")) {
                continue;
            }
            String className = jarName.replace(".class", "").replaceAll("/", ".");
            if (excludePackage.contains(className.replaceAll("\\.[a-zA-Z$_][a-zA-Z0-9$_]*$",""))){
                continue;
            }
            //如果这个类被排除,则跳过
            if (excludeClass.contains(className)){
                continue;
            }
            try {
                Class<?> klass = Class.forName(className);
                //如果这个类是注解或者枚举或者接口或者八大基本类型就跳过
                if (klass.isAnnotation()
                        || klass.isEnum()
                        || klass.isInterface()
                        || klass.isPrimitive()) {
                    continue;
                }
                //调用抽象类
                accept(klass);
            } catch (ClassNotFoundException | java.lang.NoClassDefFoundError ignored) {
            }
        }
    }

    //对包扫描方法重载，可以扫描多个包
    public void scanPacket(String[] packetNames) {
        for (String packetName : packetNames) {
            scanPacket(packetName);
        }
    }

    //对包扫描方法重载，提供多个类名，可以扫描该类所在的包
    public void scanPacket(Class<?>[] classes) {
        for (Class<?> clazz : classes) {
            scanPacket(clazz);
        }
    }

    public void scanPacket(Class<?> clazz) {
        String path = clazz.getPackage().getName();
        scanPacket(path);
    }

    public void scanPacket(String packetName) {
        String packetPath = packetName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(packetPath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar")) {
                    scanJarPacket(url);
                } else {
                    File file = new File(url.toURI());
                    if (!file.exists()) {
                        continue;
                    }
                    packetScanner(file, packetName);
                }
            }
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    protected List<Predicate<Class<?>>> getFilter() {
        return filter;
    }

    public PackageScanner filter(Predicate<Class<?>> predicate) {
        filter.add(predicate);
        return this;
    }

    public static Predicate<Class<?>> hasAnnotation(Class<? extends Annotation> annotationClazz) {
        return (c) -> c.getAnnotation(annotationClazz) != null;
    }

    public static Predicate<Class<?>> is(Class<?> target) {
        return new Predicate<>() {
            @Override
            public boolean test(Class<?> clazz) {
                if (clazz == target) return true;
                if (clazz == Object.class) return false;
                if (clazz.isInterface()) {
                    return testInterfaces(clazz.getInterfaces());
                } else {
                    if (testInterfaces(clazz.getInterfaces())) return true;
                    return test(clazz.getSuperclass());
                }
            }

            private boolean testInterfaces(Class<?>[] interfaces) {
                for (Class<?> Interface : interfaces) {
                    if (test(Interface)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    //扫描器构造方案,传入消费者,扫描出来的类将提供消费
    public static PackageScanner build(Consumer<Class<?>> clazzConsumer) {
        return new PackageScanner() {
            @Override
            public void accept(Class<?> clazz) {
                if (this.getFilter().stream().reduce(Predicate::and).orElse((c) -> true).test(clazz)) {
                    clazzConsumer.accept(clazz);
                }
            }
        };
    }
}
