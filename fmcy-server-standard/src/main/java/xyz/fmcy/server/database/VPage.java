package xyz.fmcy.server.database;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/***
 * @author 付高宏
 * @date 2023/1/28 10:19
 */
public class VPage<T> implements Serializable, Iterable<T>{
    @Serial
    private static final long serialVersionUID = 2023012810190L;
    private List<T> records;
    private long total;
    private long size;
    private long current;
    public VPage() {
    }

    public VPage(List<T> records, Long total, Long size, Long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }

    @Override
    public String toString() {
        return "Page{" +
                "records=" + records +
                ", total=" + total +
                ", size=" + size +
                ", current=" + current +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VPage<?> page)) return false;
        return Objects.equals(records, page.records) && Objects.equals(total, page.total) && Objects.equals(size, page.size) && Objects.equals(current, page.current);
    }

    @Override
    public Iterator<T> iterator() {
        return records.iterator();
    }

    @Override
    public int hashCode() {
        return Objects.hash(records, total, size, current);
    }
    public List<T> getRecords() {
        return records;
    }

    public VPage<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public VPage<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    public long getSize() {
        return size;
    }

    public VPage<T> setSize(long size) {
        this.size = size;
        return this;
    }

    public long getCurrent() {
        return current;
    }

    public VPage<T> setCurrent(long current) {
        this.current = current;
        return this;
    }
}
