package xyz.fmcy.server.database;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * XService对IService的功能进行了收缩,排除掉了一些不常用的功能
 *
 * @author 付高宏
 * @date 2023/2/9 15:32
 */
public interface XService<D> {

    Class<D> resultclass();

    /**
     * 获取列名
     *
     * @param filter 过滤条件
     */
    String[] columns(Predicate<String> filter);

    /**
     * 获取全部列名
     */
    default String[] columns() {
        return columns(column -> true);
    }

    /**
     * 排除几个列名
     *
     * @param columns 列名
     */

    default String[] columnsExcept(String... columns) {
        return columns(Predicate.not(Arrays.stream(columns).toList()::contains));
    }

    default D findById(Serializable id) {
        return findById(id, columns());
    }

    D findById(Serializable id, String... columns);

    default D find(D entity) {
        return find(entity, columns());
    }

    D find(D entity, String... columns);

    default VPage<D> findPage(Long current, Long size, D template, QueryConfigure... configures) {
        return findPage(current, size, template, configures, columns());
    }

    VPage<D> findPage(Long current, Long size, D template, QueryConfigure[] configures, String... columns);

    default <C extends QueryConfiguration & Serializable> VPage<D> findPage(PageSeed<C> pageSeed) {
        return findPage(pageSeed, columns());
    }

    <C extends QueryConfiguration & Serializable> VPage<D> findPage(PageSeed<C> pageSeed, String... columns);

    default <C extends QueryConfiguration & Serializable> List<D> findList(QuerySeed<C> querySeed) {
        return findList(querySeed, columns());
    }

    <C extends QueryConfiguration & Serializable> List<D> findList(QuerySeed<C> querySeed, String... columns);

    default Long count() {
        return count(null);
    }

    Long count(D template);
    default List<D> findList(D template, QueryConfigure... configures) {
        return findList(template, configures, columns());
    }

    List<D> findList(D template, QueryConfigure[] configures, String... columns);

    boolean add(D entity);

    default List<D> findListByIds(Serializable... ids) {
        return findListByIds(ids, columns());
    }

    List<D> findListByIds(Serializable[] ids, String... columns);

    boolean addAll(Collection<D> collection, boolean cautious);

    default boolean addAll(Collection<D> collection) {
        return addAll(collection, true);
    }

    boolean deleteById(Serializable id);

    boolean deleteById(D entity);

    boolean delete(D template);

    boolean deleteByIds(Collection<Serializable> ids);

    boolean updateById(D entity);

    boolean update(D setter, D where, String... likeFields);
}
