package xyz.fmcy.mybatisplus.util.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.fmcy.server.database.VPage;
import xyz.fmcy.util.entity.DemandHandler;
import xyz.fmcy.util.entity.Demands;
import xyz.fmcy.util.entity.EntityMapper;

public class PageEntityMapper {
    public static <O, T> VPage<T> mapper(IPage<O> page, DemandHandler<O, T> handler) {
        return EntityMapper.getMapper(() -> page, VPage<T>::new)
                .want(Demands.mapMove(IPage::getRecords, handler::mapAll, VPage::setRecords))
                .wiseMapping(page);
    }

    public static <T> VPage<T> mapper(IPage<T> page) {
        VPage<T> vPage = new VPage<>();
        vPage.setRecords(page.getRecords());
        vPage.setCurrent(page.getCurrent());
        vPage.setSize(page.getSize());
        vPage.setTotal(page.getTotal());
        return vPage;
    }


    public static <O, T> VPage<T> mapper(VPage<O> page, DemandHandler<O, T> handler) {
        return EntityMapper.getMapper(() -> page, VPage<T>::new)
                .want(Demands.mapMove(VPage::getRecords, handler::mapAll, VPage::setRecords))
                .wiseMapping(page);
    }

    public static <O, T> IPage<T> mapper(DemandHandler<O, T> handler, IPage<O> page) {
        return EntityMapper.getMapper(() -> page, Page<T>::new)
                .want(Demands.mapMove(IPage::getRecords, handler::mapAll, Page::setRecords))
                .wiseMapping(page);
    }

    public static <O, T> IPage<T> mapper(DemandHandler<O, T> handler, VPage<O> page) {
        return EntityMapper.getMapper(() -> page, Page<T>::new)
                .want(Demands.mapMove(VPage::getRecords, handler::mapAll, Page::setRecords))
                .wiseMapping(page);
    }


    public static <T> IPage<T> mapper(VPage<T> page) {
        IPage<T> iPage = new Page<>();
        iPage.setRecords(page.getRecords());
        iPage.setCurrent(page.getCurrent());
        iPage.setSize(page.getSize());
        iPage.setTotal(page.getTotal());
        return iPage;
    }
}
