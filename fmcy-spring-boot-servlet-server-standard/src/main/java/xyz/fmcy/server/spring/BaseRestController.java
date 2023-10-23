package xyz.fmcy.server.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import xyz.fmcy.entity.E;
import xyz.fmcy.server.database.*;
import xyz.fmcy.server.spring.core.ServerResultCode;
import xyz.fmcy.server.standard.Message;
import xyz.fmcy.server.standard.Result;
import xyz.fmcy.util.entity.DemandHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RestController
public abstract class BaseRestController<T> {
    private XService<T> xService;
    private Class<T> resultClass;
    @SuppressWarnings("unchecked")
    private final Class<? extends BaseRestController<T>> controllerClass = (Class<? extends BaseRestController<T>>) this.getClass();

    @Autowired(required = false)
    public void setService(XService<T> service) {
        this.xService = service;
        this.resultClass = xService.resultclass();
    }

    public XService<T> getService() {
        return xService;
    }

    protected Result<?> findById(Serializable id, Class<?> targetType) {
        T result = xService.findById(id);
        return Result.success(Object.class.equals(targetType) ? result
                : E.getMapper(resultClass, targetType).wiseMapping(result));
    }

    protected Result<?> findList(QuerySeed<? extends QueryConfiguration> querySeed, Class<?> targetType) {
        List<T> list = xService.findList(querySeed);
        return Result.success(Object.class.equals(targetType) ? list :
                E.getMapper(resultClass, targetType).mapAll(list)
        );
    }


    @SuppressWarnings("unchecked")
    protected Result<?> addList(List<Serializable> list) {
        boolean add = true;
        if (!list.isEmpty()) {
            List<T> tList;
            if (getService().resultclass().equals(list.get(0).getClass())) {
                tList = (List<T>) list;
            } else {
                DemandHandler<Serializable, T> mapper = (DemandHandler<Serializable, T>) E.getMapper(list.get(0).getClass(), resultClass);
                tList = mapper.mapAll(list);
            }
            add = xService.addAll(tList);
        }
        return add ? Result.success(null, Message.success(ServerResultCode.ADD_SUCCESS))
                : Result.error(ServerResultCode.ADD_FAIL);
    }


    protected Result<?> findPage(PageSeed<? extends QueryConfiguration> querySeed, Class<?> targetType) {
        VPage<T> page = xService.findPage(querySeed);
        if (Object.class.equals(targetType)) {
            return Result.success(page);
        }
        List<?> objects = E.getMapper(resultClass, targetType).mapAll(page.getRecords());
        VPage<Object> vpage = new VPage<>();
        vpage.setCurrent(page.getCurrent());
        vpage.setRecords(new ArrayList<>(objects));
        vpage.setSize(page.getSize());
        vpage.setTotal(page.getTotal());
        return Result.success(vpage);
    }

    @SuppressWarnings("unchecked")
    protected Result<?> updateById(Serializable updater) {
        Class<? extends Serializable> updaterClass = updater.getClass();
        T t = getService().resultclass().equals(updaterClass) ? (T) updater : ((DemandHandler<Serializable, T>) E.getMapper(updaterClass, resultClass)).wiseMapping(updater);
        return getService().updateById(t) ? Result.success(null, Message.success(ServerResultCode.UPDATE_SUCCESS)) : Result.error(ServerResultCode.UPDATE_FAIL);
    }


    @SuppressWarnings("unchecked")
    protected Result<?> addOne(Serializable insert) {
        Class<? extends Serializable> insertClass = insert.getClass();
        T t = getService().resultclass().equals(insertClass) ? (T) insert : ((DemandHandler<Serializable, T>) E.getMapper(insertClass, resultClass)).wiseMapping(insert);
        return xService.add(t) ? Result.success(null, Message.success(ServerResultCode.ADD_SUCCESS)) :
                Result.error(ServerResultCode.ADD_FAIL);
    }

    protected Result<?> deleteById(Serializable id) {
        return xService.deleteById(id) ? Result.success(null, Message.success(ServerResultCode.DELETE_SUCCESS)) : Result.error(ServerResultCode.DELETE_FAIL);
    }
}
