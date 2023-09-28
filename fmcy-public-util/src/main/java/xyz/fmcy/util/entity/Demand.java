package xyz.fmcy.util.entity;

/**
 * @author 付高宏
 * @date 2023/1/23 14:51
 */
@FunctionalInterface
public interface Demand<O, T> {
    /**
     * 执行这个需求
     * @param o 原对象
     * @param t 目标对象
     */
    void perform(O o, T t);
}
