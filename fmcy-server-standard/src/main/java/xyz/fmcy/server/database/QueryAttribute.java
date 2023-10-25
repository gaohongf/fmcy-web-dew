package xyz.fmcy.server.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryAttribute<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 20230905233719L;
    private T value;
    private boolean like;
    private boolean asc;
    private boolean desc;
    private List<T> nes;
    private List<QueryScope<T>> scope;

    public QueryAttribute() {
    }

    public QueryAttribute(T value, boolean like, boolean asc, boolean desc, List<T> nes, List<QueryScope<T>> scope) {
        this.value = value;
        this.like = like;
        this.asc = asc;
        this.desc = desc;
        this.nes = nes;
        this.scope = scope;
    }

    public void setNe(List<T> nes) {
        this.nes = nes;
    }

    public List<T> getNe() {
        return nes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryAttribute<?> that = (QueryAttribute<?>) o;
        return like == that.like && asc == that.asc && desc == that.desc && Objects.equals(value, that.value) && Objects.equals(nes, that.nes) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, like, asc, desc, nes, scope);
    }

    public List<QueryScope<T>> getScope() {
        return scope;
    }

    public void setScopes(List<QueryScope<T>> scope) {
        if (this.scope != null) {
            this.scope.addAll(scope);
        } else {
            this.scope = new ArrayList<>(scope);
        }

    }

    public void setScope(QueryScope<T> scope) {
        if (this.scope != null) {
            this.scope.add(scope);
        } else {
            this.scope = new ArrayList<>(List.of(scope));
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "QueryAttribute{" +
                "value=" + value +
                ", like=" + like +
                ", asc=" + asc +
                ", desc=" + desc +
                ", nes=" + nes +
                ", scope=" + scope +
                '}';
    }
}
