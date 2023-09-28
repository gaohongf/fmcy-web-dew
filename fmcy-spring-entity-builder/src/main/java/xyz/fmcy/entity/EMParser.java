package xyz.fmcy.entity;

import xyz.fmcy.entity.annotation.EM;

public interface EMParser {
    void runMapping(Class<?> source, EM em);
}
