package xyz.fmcy.server.spring.core;

import java.lang.annotation.Annotation;

@SuppressWarnings("ClassExplicitlyAnnotation")
public class AnnotationsAnnotation implements Annotations {
    private Class<? extends Annotation> type;
    private Field[] fields;

    public AnnotationsAnnotation(Field[] fields, Class<? extends Annotation> type) {
        this.fields = fields;
        this.type = type;
    }

    public AnnotationsAnnotation(Class<? extends Annotation> value) {
        this(new Field[0], value);
    }

    public AnnotationsAnnotation() {
        this(new Field[0], Annotation.class);
    }

    public void setFields(Field[] fields) {
        if (fields != null) {
            this.fields = fields;
        }
    }

    public void setType(Class<? extends Annotation> type) {
        if (type != null) {
            this.type = type;
        }
    }

    @Override
    public Class<? extends Annotation> type() {
        return type;
    }

    @Override
    public Field[] fields() {
        return fields;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Annotations.class;
    }
}
