package xyz.fmcy.server.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 分页查询种子
 * 以此为基准进行分页查询
 */
public class PageSeed<T extends QueryConfiguration & Serializable> extends QuerySeed<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2023051609080L;
    /**
     * 页码
     */
    private Long current;
    /**
     * 长度
     */
    private Long size;

    /**
     * 模板
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageSeed<?> pageSeed = (PageSeed<?>) o;
        return Objects.equals(current, pageSeed.current)
                && Objects.equals(size, pageSeed.size)
                && Objects.equals(getTemplate(), pageSeed.getTemplate()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, size, getTemplate());
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public PageSeed<T> currentAndSizeNotNull() {
        if (current == null) current = 1L;
        if (size == null) size = 1L;
        return this;
    }

    public PageSeed<T> currentAndSizeNotNull(Long current, Long size) {
        if (this.current == null) this.current = current;
        if (this.size == null) this.size = size;
        return this;
    }

    @Override
    public String toString() {
        return "PageSeed{" +
                "current=" + current +
                ", size=" + size +
                ", template=" + getTemplate() +
                "} ";
    }
}
