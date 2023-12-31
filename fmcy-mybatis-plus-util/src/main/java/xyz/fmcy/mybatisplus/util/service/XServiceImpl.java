package xyz.fmcy.mybatisplus.util.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.conditions.interfaces.Func;
import com.baomidou.mybatisplus.core.conditions.interfaces.Nested;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import xyz.fmcy.mybatisplus.util.annotation.EMXService;
import xyz.fmcy.mybatisplus.util.page.*;
import xyz.fmcy.server.database.*;
import xyz.fmcy.util.entity.DemandHandler;
import xyz.fmcy.util.entity.EntityMapper;
import xyz.fmcy.util.entity.auto.EntityMapperPool;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author 付高宏
 * @date 2023/2/9 15:51
 */
@Component
public abstract class XServiceImpl<M extends BaseMapper<P>, P, D> implements XService<D> {
    @Autowired
    private M baseMapper;
    @Autowired
    @Lazy
    private EntityMapperPool entityMapperPool;
    private DemandHandler<P, D> demandHandlerPToD;
    private DemandHandler<D, P> demandHandlerDToP;
    private Map<String, ColumnCache> tableColumnMap;
    private TableInfo tableInfo;
    private String[] allTableColumn;

    @SuppressWarnings("unchecked")
    public DemandHandler<P, D> getDemandHandlerPToD() {
        EMXService emxService = this.getClass().getAnnotation(EMXService.class);
        return demandHandlerPToD != null ? demandHandlerPToD :
                (demandHandlerPToD = (DemandHandler<P, D>) entityMapperPool
                        .mapper(emxService.source(), emxService.target())
                );
    }

    @SuppressWarnings("unchecked")
    public DemandHandler<D, P> getDemandHandlerDToP() {
        EMXService emxService = this.getClass().getAnnotation(EMXService.class);
        return demandHandlerDToP != null ? demandHandlerDToP : (demandHandlerDToP = (DemandHandler<D, P>) entityMapperPool
                .mapper(emxService.target(), emxService.source())
        );
    }

    @Override
    public Class<D> resultclass() {
        return getDemandHandlerDToP().getSourceClass();
    }

    public M getBaseMapper() {
        return baseMapper;
    }

    @Override
    public boolean deleteById(Serializable id) {
        return SqlHelper.retBool(getBaseMapper().deleteById(id));
    }

    @Override
    public boolean deleteById(D entity) {
        return SqlHelper.retBool(
                getBaseMapper().deleteById(getDemandHandlerDToP().wiseMapping(entity))
        );
    }

    @Override
    public boolean updateById(D entity) {
        return SqlHelper.retBool(
                getBaseMapper().updateById(getDemandHandlerDToP().wiseMapping(entity))
        );
    }

    private TableInfo getTableInfo() {
        return tableInfo != null ? tableInfo : (tableInfo = TableInfoHelper.getTableInfo(getDemandHandlerPToD().getSourceClass()));
    }

    private Map<String, ColumnCache> getTableColumnMap() {
        return tableColumnMap != null ? tableColumnMap : (tableColumnMap = LambdaUtils.getColumnMap(getDemandHandlerPToD().getSourceClass()));
    }

    private String[] allTableColumn() {
        return allTableColumn != null ? allTableColumn : (allTableColumn = getTableColumnMap().values().stream().map(ColumnCache::getColumn).toArray(String[]::new));
    }

    @Override
    public String[] columns(Predicate<String> filter) {
        return Arrays.stream(allTableColumn()).filter(filter).toArray(String[]::new);
    }

    private String[] columnsToColumnSelects(String... columns) {
        Map<String, String> columnsColumnSelectMap = getTableColumnMap().values().stream().collect(Collectors.toMap(ColumnCache::getColumn, ColumnCache::getColumnSelect));
        return Arrays.stream(columns).map(columnsColumnSelectMap::get).toArray(String[]::new);
    }

    @Override
    public D findById(Serializable id, String... columns) {
        return Optional.ofNullable(getBaseMapper().selectOne(Wrappers.<P>query()
                        .eq(getTableInfo().getKeyColumn(), id)
                        .select(columnsToColumnSelects(columns))
                )).map(getDemandHandlerPToD()::wiseMapping)
                .orElse(null);
    }

    @Override
    public D find(D entity, String... columns) {
        return Optional.ofNullable(getBaseMapper().selectOne(loadWrapper(Wrappers.query(), entity).select(columnsToColumnSelects(columns))))
                .map(getDemandHandlerPToD()::wiseMapping)
                .orElse(null);
    }

    @Override
    public VPage<D> findPage(Long current, Long size, D template, QueryConfigure[] configures, String... columns) {
        return PageEntityMapper.mapper(getBaseMapper()
                        .selectPage(Page.of(current, size), loadWrapper(Wrappers.query(), template, configures)
                                .select(columnsToColumnSelects(columns)))
                , getDemandHandlerPToD());
    }

    @Override
    public <C extends QueryConfiguration & Serializable> VPage<D> findPage(PageSeed<C> pageSeed, String... columns) {
        AtomicReference<VPage<D>> page = new AtomicReference<>();
        Long current = pageSeed.getCurrent();
        Long size = pageSeed.getSize();
        readQuerySend(pageSeed, (template, configures) -> page
                .set(configures == null
                        ? findPage(current, size, template, new QueryConfigure[0], columns)
                        : findPage(current, size, template, configures, columns)
                )
        );
        return page.get();
    }

    @Override
    public <C extends QueryConfiguration & Serializable> List<D> findList(QuerySeed<C> querySeed, String... columns) {
        AtomicReference<List<D>> list = new AtomicReference<>();
        readQuerySend(querySeed, (template, configures) -> list
                .set(configures == null
                        ? findList(template, new QueryConfigure[0], columns)
                        : findList(template, configures, columns)
                )
        );
        return list.get();
    }

    @Override
    public Long count(D template) {
        return getBaseMapper().selectCount(loadWrapper(Wrappers.query(), template));
    }

    @Override
    public List<D> findList(D template, QueryConfigure[] configures, String... columns) {
        return getDemandHandlerPToD().mapAll(
                getBaseMapper().selectList(loadWrapper(Wrappers.query(), template, configures).select(columnsToColumnSelects(columns)))
        );
    }


    @SuppressWarnings("unchecked")
    protected <C extends QueryConfiguration & Serializable> void readQuerySend(QuerySeed<C> querySeed, BiConsumer<D, QueryConfigure[]> consumer) {
        Optional.ofNullable(querySeed.getTemplate())
                .ifPresentOrElse(template -> {
                    DemandHandler<C, D> handler = entityMapperPool.mapper(
                            (Class<C>) template.getClass(),
                            getDemandHandlerDToP().getSourceClass()
                    );
                    if (handler == null) {
                        handler = EntityMapper.getMapper((Class<C>) template.getClass(), getDemandHandlerDToP().getSourceClass());
                    }
                    DemandHandler<C, D> finalHandler = handler;
                    Optional.ofNullable(template.configure())
                            .ifPresentOrElse(
                                    configures -> consumer.accept(finalHandler.wiseMapping(template), configures),
                                    () -> consumer.accept(finalHandler.wiseMapping(template), null)
                            );
                }, () -> consumer.accept(null, null));
    }

    @SuppressWarnings("unchecked")
    protected <W extends Wrapper<P>> W loadWrapper(W wrapper, D template, QueryConfigure... configures) {
        if (wrapper instanceof QueryWrapper<?> queryWrapper) {
            return (W) interpretQueryTemplate((QueryWrapper<P>) queryWrapper, template, configures);
        } else if (wrapper instanceof UpdateWrapper<?> updateWrapper) {
            return (W) interpretUpdateTemplate((UpdateWrapper<P>) updateWrapper, template);
        } else {
            if (wrapper instanceof Compare) {
                interpretCompareTemplate((Compare<W, String>) wrapper, template, configures);
            }
            if (wrapper instanceof Func) {
                interpretFuncTemplate((Func<W, String>) wrapper, template, configures);
            }
            return wrapper;
        }
    }

    private <W extends Wrapper<P>> void interpretCompareTemplate(Compare<W, String> compare, D template, QueryConfigure... configures) {
        Map<String, QueryConfigure> fieldConfigs = Arrays.stream(configures).collect(Collectors.toMap(QueryConfigure::getFieldName, configure -> configure));
        interpretTemplate(template, ((p, field) -> {
            try {
                field.setAccessible(true);
                Object o = field.get(p);
                String name = field.getName();
                ColumnCache columnCache = getTableColumnMap().get(LambdaUtils.formatKey(name));
                String column = columnCache.getColumn();
                QueryConfigure configure = fieldConfigs.get(name);
                loadCompare(compare, o, column, configure);
            } catch (IllegalAccessException ignored) {
            }
        }));
    }

    private <W> void interpretFuncTemplate(Func<W, String> func, D template, QueryConfigure... configures) {
        Map<String, QueryConfigure> fieldConfigs = Arrays.stream(configures).collect(Collectors.toMap(QueryConfigure::getFieldName, configure -> configure));
        interpretTemplate(template, ((p, field) -> {
            String name = field.getName();
            ColumnCache columnCache = getTableColumnMap().get(LambdaUtils.formatKey(name));
            String column = columnCache.getColumn();
            QueryConfigure configure = fieldConfigs.get(name);
            loadFunc(func, column, configure);
        }));
    }

    protected void loadFunc(Func<?, String> func, String column, QueryConfigure configure) {
        if (configure != null) {
            List<?> nes = Optional.ofNullable(configure.getNes()).orElseGet(ArrayList::new);
            func.notIn(!nes.isEmpty(), column, nes);
            if (configure.isAsc()) {
                func.orderByAsc(column);
            } else if (configure.isDesc()) {
                func.orderByDesc(column);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <W extends Wrapper<P>, N extends Nested<N, N> & Compare<W, String>> void loadCompare(
            Compare<W, String> compare, Object o, String column, QueryConfigure configure
    ) {
        if (configure != null) {
            boolean like = configure.isLike();
            compare.like(like && o != null, column, o);
            List<QueryScope<Object>> scopes = configure.getScope();
            if (scopes != null) {
                Nested<N, N> nested = compare instanceof Nested ? (Nested<N, N>) compare : null;
                if (nested != null) {
                    // ??? and (value > ? and value < ?) or (value > ? and value < ?) and ???
                    nested.and((n) -> scopes.forEach((scope) -> n.or(w -> loadScope(scope, w, column))));
                } else if (!scopes.isEmpty()) loadScope(scopes.get(0), compare, column);
            } else compare.eq(!like && o != null, column, o);
        } else compare.eq(o != null, column, o);
    }

    private <W extends Wrapper<P>> void loadScope(QueryScope<Object> scope, Compare<W, String> compare, String column) {
        compare.gt(scope.isGt(), column, scope.getLowerLimit());
        compare.ge(scope.isGe(), column, scope.getLowerLimit());
        compare.le(scope.isLe(), column, scope.getUpperLimit());
        compare.lt(scope.isLt(), column, scope.getUpperLimit());
    }

    private QueryWrapper<P> interpretQueryTemplate(QueryWrapper<P> wrapper, D template, QueryConfigure... configures) {
        Map<String, QueryConfigure> fieldConfigs = Arrays.stream(configures).collect(Collectors.toMap(QueryConfigure::getFieldName, configure -> configure));
        interpretTemplate(template, ((p, field) -> {
            try {
                field.setAccessible(true);
                Object o = field.get(p);
                String name = field.getName();
                ColumnCache columnCache = getTableColumnMap().get(LambdaUtils.formatKey(name));
                String column = columnCache.getColumn();
                QueryConfigure configure = fieldConfigs.get(name);
                loadCompare(wrapper, o, column, configure);
                loadFunc(wrapper, column, configure);
            } catch (IllegalAccessException ignored) {
            }
        }));
        return wrapper;
    }

    private UpdateWrapper<P> interpretUpdateTemplate(UpdateWrapper<P> wrapper, D template) {
        interpretTemplate(template, ((p, field) -> {
            try {
                field.setAccessible(true);
                Object o = field.get(p);
                ColumnCache columnCache = getTableColumnMap().get(LambdaUtils.formatKey(field.getName()));
                wrapper.eq(o != null, columnCache.getColumn(), o);
            } catch (IllegalAccessException ignored) {
            }
        }));
        return wrapper;
    }

    protected void interpretTemplate(D template, BiConsumer<P, Field> consumer) {
        if (template == null) return;
        DemandHandler<D, P> demandHandlerDToP = getDemandHandlerDToP();
        Class<P> targetClass = demandHandlerDToP.getTargetClass();
        P p = demandHandlerDToP.wiseMapping(template);
        Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> !"serialVersionUID".equals(field.getName()))
                .forEach(field -> consumer.accept(p, field));
    }

    @Override
    public boolean add(D entity) {
        P p = getDemandHandlerDToP().wiseMapping(entity);
        boolean flag = SqlHelper.retBool(getBaseMapper().insert(p));
        getDemandHandlerPToD().wiseMapping(p, entity);
        return flag;
    }

    @Override
    @Transactional
    public boolean deleteByIds(Collection<Serializable> ids) {
        return Optional.ofNullable(ids)
                .map(e -> getBaseMapper().deleteBatchIds(ids) > 0)
                .orElse(false);
    }

    @Override
    public List<D> findListByIds(Serializable... ids) {
        return Optional.ofNullable(ids)
                .map(i -> getBaseMapper().selectBatchIds(Arrays.stream(i).collect(Collectors.toList())))
                .map(getDemandHandlerPToD()::mapAll)
                .orElseGet(ArrayList::new);
    }

    @Override
    public List<D> findListByIds(Serializable[] ids, String... columns) {
        return Optional.ofNullable(ids)
                .filter(i -> i.length != 0)
                .map(i -> getBaseMapper().selectList(Wrappers.<P>query()
                        .select(columnsToColumnSelects(columns))
                        .in(getTableInfo().getKeyColumn(), Arrays.stream(ids).toList())
                )).map(getDemandHandlerPToD()::mapAll)
                .orElseGet(ArrayList::new);
    }

    @Override
    @Transactional
    public boolean addAll(Collection<D> collection, boolean cautious) {
        return collection.stream()
                .map(this::add)
                .reduce((b1, b2) -> b1 & b2)
                .stream().peek(b -> {
                    if (!b && cautious) TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                }).findFirst()
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(D template) {
        return getBaseMapper().delete(loadWrapper(Wrappers.query(), template)) > 0;
    }

    @Override
    public boolean update(D setter, D where, String... likeField) {
        return SqlHelper.retBool(getBaseMapper()
                .update(getDemandHandlerDToP().wiseMapping(setter), loadWrapper(Wrappers.query(), where, Arrays.stream(likeField)
                        .map(QueryConfigure::like).toArray(QueryConfigure[]::new))
                )
        );
    }
}
