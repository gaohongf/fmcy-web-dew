package xyz.fmcy.util.entity.auto;

import xyz.fmcy.util.entity.DemandReceiver;

/**
 * @author 付高宏
 * @date 2023/1/31 11:53
 */
public interface EntityMapperHandler<O, T> {

    void handle(DemandReceiver<O, T> demandReceiver);
}
