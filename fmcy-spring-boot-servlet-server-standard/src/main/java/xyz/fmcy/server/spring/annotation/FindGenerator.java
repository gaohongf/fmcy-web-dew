package xyz.fmcy.server.spring.annotation;

import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.spring.abandon.QueryProxy;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FindGenerator {
    Class<? extends QueryConfiguration> query() default QueryProxy.class;

    EnableFindById enableFindById() default @EnableFindById;

    EnableList enableList() default @EnableList;

    EnablePage enablePage() default @EnablePage;

    @interface EnableFindById {

        boolean value() default true;

        FindById annotation() default @FindById;
    }

    @interface EnablePage {

        boolean value() default true;

        FindPage annotation() default @FindPage(query = QueryProxy.class);
    }

    @interface EnableList {

        boolean value() default true;

        FindList annotation() default @FindList(query = QueryProxy.class);
    }

    @SuppressWarnings({"ClassExplicitlyAnnotation"})
    class FindGeneratorProxy implements FindGenerator {

        private Class<? extends QueryConfiguration> query = QueryProxy.class;

        public void setQuery(Class<? extends QueryConfiguration> query) {
            this.query = query;
        }

        @Override
        public Class<? extends QueryConfiguration> query() {
            return query;
        }

        @Override
        public EnableFindById enableFindById() {
            try {
                return (EnableFindById) FindGenerator.class.getMethod("enableFindById").getDefaultValue();
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @Override
        public EnableList enableList() {
            try {
                return (EnableList) FindGenerator.class.getMethod("enableList").getDefaultValue();
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @Override
        public EnablePage enablePage() {
            try {
                return (EnablePage) FindGenerator.class.getMethod("enablePage").getDefaultValue();
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return FindGenerator.class;
        }
    }
}



