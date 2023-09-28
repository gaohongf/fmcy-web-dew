package xyz.fmcy.entity;

import java.util.Map;

@FunctionalInterface
public interface SimpleClassNameMap {
    Map<String,Class<?>> getClassMap();
}
