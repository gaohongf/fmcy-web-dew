package xyz.fmcy.server.spring.core;

import xyz.fmcy.server.spring.annotation.proxy.AnnotationProxy;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import xyz.fmcy.server.spring.core.Annotations.Field;

public class AnnotationsProxy {
    private Class<? extends Annotation> type;
    private Field[] fields;

    public AnnotationsProxy(Field[] fields, Class<? extends Annotation> type) {
        this.fields = fields;
        this.type = type;
    }

    public AnnotationsProxy(Class<? extends Annotation> value) {
        this(new Field[0], value);
    }

    public AnnotationsProxy() {
        this(new Field[0], Annotation.class);
    }

    public void setFields(List<Map<String, Object>> fields) {
        this.fields = fields.stream()
                .map(field -> AnnotationProxy.proxy(Field.class)
                        .modify("name", field.get("name")).modify("value", field.get("value"))
                        .get()
                ).toArray(Field[]::new);
    }

    public void setType(Class<? extends Annotation> type) {
        if (type != null) {
            this.type = type;
        }
    }

    public Class<? extends Annotation> type() {
        return type;
    }

    public Annotations getProxy() {
        return AnnotationProxy.proxy(Annotations.class).modify("type", type).modify("fields", fields).get();
    }
}
