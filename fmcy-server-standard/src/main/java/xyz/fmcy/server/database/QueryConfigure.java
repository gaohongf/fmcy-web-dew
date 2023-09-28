package xyz.fmcy.server.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class QueryConfigure implements Serializable {
    @Serial
    private static final long serialVersionUID = 2023051615350L;
    private String fieldName;
    private boolean like;
    private boolean asc;
    private boolean desc;
    private QueryScope<?> scope;

    public String getFieldName() {
        return fieldName;
    }

    public void setScope(QueryScope<?> scope) {
        this.scope = scope;
    }

    public QueryScope<?> getScope() {
        return scope;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    private static QueryConfigureBuilder builder() {
        return new QueryConfigureBuilder();
    }

    private static QueryConfigure of(boolean like, String fieldName, boolean asc, boolean desc) {
        return of(like, fieldName, asc, desc, null);
    }

    private static QueryConfigure of(boolean like, String fieldName, boolean asc, boolean desc, QueryScope<?> scope) {
        return builder().fieldName(fieldName).asc(asc).like(like).desc(desc).scope(scope).build();
    }

    public static QueryConfigure setAttribute(String fieldName, QueryAttribute<?> attribute) {
        QueryConfigureBuilder builder = builder().fieldName(fieldName);
        if (attribute != null) {
            builder.like(attribute.isLike());
            if (attribute.isAsc()) {
                builder.asc(true);
            }
            if (!attribute.isAsc() && attribute.isDesc()) {
                builder.desc(true);
            }
            builder.scope(attribute.getScope());
        }
        return builder.build();
    }

    public static QueryConfigure like(String fieldName) {
        return of(true, fieldName, false, false);
    }

    public static QueryConfigure asc(String fieldName) {
        return of(false, fieldName, true, false);
    }

    public static QueryConfigure desc(String fieldName) {
        return of(false, fieldName, false, true);
    }

    public static QueryConfigure likeAndAsc(String fieldName) {
        return of(true, fieldName, true, false);
    }

    public static QueryConfigure likeAndDesc(String fieldName) {
        return of(true, fieldName, false, true);
    }

    public static QueryConfigure scope(String fieldName, QueryScope<?> scope) {
        return of(false, fieldName, false, false, scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryConfigure that = (QueryConfigure) o;
        return like == that.like && asc == that.asc && desc == that.desc && Objects.equals(fieldName, that.fieldName) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, like, asc, desc, scope);
    }

    private static class QueryConfigureBuilder {
        private final QueryConfigure queryConfigure;

        QueryConfigureBuilder() {
            queryConfigure = new QueryConfigure();
        }

        public QueryConfigureBuilder asc(boolean asc) {
            queryConfigure.asc = asc;
            return this;
        }

        public QueryConfigureBuilder desc(boolean desc) {
            queryConfigure.desc = desc;
            return this;
        }

        public QueryConfigureBuilder like(boolean like) {
            queryConfigure.like = like;
            return this;
        }

        public QueryConfigureBuilder scope(QueryScope<?> scope) {
            queryConfigure.scope = scope;
            return this;
        }

        public QueryConfigureBuilder fieldName(String fieldName) {
            queryConfigure.fieldName = fieldName;
            return this;
        }

        public QueryConfigure build() {
            return queryConfigure;
        }
    }
}
