package xyz.fmcy.server.database;

import java.io.Serial;
import java.io.Serializable;

public class QuerySeed<T extends QueryConfiguration & Serializable>implements Serializable {
    @Serial
    private static final long serialVersionUID = 2023051614540L;

    private T template;

    public void setTemplate(T template) {
        this.template = template;
    }

    public T getTemplate() {
        return template;
    }
}
