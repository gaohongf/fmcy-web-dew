package xyz.fmcy.util.entity;

/**
 * @author 付高宏
 * @date 2023/1/29 16:18
 */
@FunctionalInterface
public interface ExcludeAutoField<O,T> extends Demand<O,T>{
    @Override
    default void perform(O o, T t){
    }

    String exclude();
}
