package xyz.fmcy.server.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class QueryScope<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 20230905233953L;
    private T upperLimit;
    private boolean equalsUpperLimit;
    private T lowerLimit;
    private boolean equalsLowerLimit;

    public boolean isGt(){
        return !equalsLowerLimit && this.lowerLimit != null ;
    }
    public boolean isLt(){
        return !equalsUpperLimit && this.upperLimit != null ;
    }

    public boolean isGe(){
        return equalsLowerLimit && this.lowerLimit != null;
    }

    public boolean isLe(){
        return equalsUpperLimit && this.upperLimit != null;
    }

    public Expander<T> greaterThan(T t) {
        this.lowerLimit = t;
        return Expander.le(this);
    }

    public Expander<T> lessThan(T t) {
        this.upperLimit = t;
        return Expander.ue(this);
    }

    public void setGt(T lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public void setLt(T upperLimit) {
        this.upperLimit = upperLimit;
    }

    public void setLe(T upperLimit) {
        this.upperLimit = upperLimit;
        this.equalsUpperLimit = true;
    }

    public void setGe(T lowerLimit) {
        this.lowerLimit = lowerLimit;
        this.equalsLowerLimit = true;
    }

    public T getLowerLimit() {
        return lowerLimit;
    }

    public T getUpperLimit() {
        return upperLimit;
    }

    public boolean isEqualsUpperLimit() {
        return equalsUpperLimit;
    }

    public boolean isEqualsLowerLimit() {
        return equalsLowerLimit;
    }

    public QueryScope() {
    }

    public QueryScope(T upperLimit, T lowerLimit) {
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryScope<?> that = (QueryScope<?>) o;
        return Objects.equals(upperLimit, that.upperLimit) && Objects.equals(lowerLimit, that.lowerLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upperLimit, lowerLimit);
    }

    public static sealed abstract class Expander<T> implements Serializable permits UpperLimitEqual, LowerLimitEqual {
        @Serial
        private static final long serialVersionUID = 20230905235923L;
        private final QueryScope<T> scope;

        public QueryScope<T> and() {
            return scope;
        }

        public abstract QueryScope<T> equal();

        public Expander(QueryScope<T> scope) {
            this.scope = scope;
        }

        public static <T> Expander<T> ue(QueryScope<T> scope) {
            return new UpperLimitEqual<>(scope);
        }

        public static <T> Expander<T> le(QueryScope<T> scope) {
            return new LowerLimitEqual<>(scope);
        }
    }

    private static final class UpperLimitEqual<T> extends Expander<T> {
        public UpperLimitEqual(QueryScope<T> scope) {
            super(scope);
        }

        @Override
        public QueryScope<T> equal() {
            super.scope.equalsUpperLimit = true;
            return super.scope;
        }
    }

    private static final class LowerLimitEqual<T> extends Expander<T> {
        public LowerLimitEqual(QueryScope<T> scope) {
            super(scope);
        }

        @Override
        public QueryScope<T> equal() {
            super.scope.equalsLowerLimit = true;
            return super.scope;
        }
    }
}
