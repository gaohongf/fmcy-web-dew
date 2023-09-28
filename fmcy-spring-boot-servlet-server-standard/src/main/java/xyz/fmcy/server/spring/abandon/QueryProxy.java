package xyz.fmcy.server.spring.abandon;

import xyz.fmcy.server.database.QueryAttribute;
import xyz.fmcy.server.database.QueryConfiguration;
import xyz.fmcy.server.database.QueryConfigure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用查询配置,通过Map实现
 */
public class QueryProxy extends HashMap<String, QueryAttribute<String>> implements QueryConfiguration, Serializable {
    @Override
    public QueryConfigure[] configure() {
        return this.entrySet()
                .stream()
                .map(entry -> {
                    QueryAttribute<String> value = entry.getValue();
                    return Map.entry(entry.getKey(), value);
                })
                .map(entry -> QueryConfigure.setAttribute(entry.getKey(), entry.getValue()))
                .toArray(QueryConfigure[]::new);
    }
}
