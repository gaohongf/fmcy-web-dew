package xyz.fmcy.util.entity;

/**
 * @author 付高宏
 * @date 2023/1/29 14:32
 */
@FunctionalInterface
public interface GroupManager<O,T> {
    Group<O,T> group(Object key);
}
