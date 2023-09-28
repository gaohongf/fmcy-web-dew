package xyz.fmcy.server.spring.core;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;

public class PathVariableAnnotation implements PathVariable {

    private String value;
    private String name;
    private boolean required;

    public PathVariableAnnotation(String value, String name, boolean required) {
        this.value = value;
        this.name = name;
        this.required = required;
    }

    public PathVariableAnnotation() {
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public @NotNull String value() {
        return value;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public boolean required() {
        return required;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return PathVariable.class;
    }
}
