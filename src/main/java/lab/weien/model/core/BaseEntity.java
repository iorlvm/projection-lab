package lab.weien.model.core;

import java.util.Objects;

/**
 * 提供符合 DDD 的 Entity 基類方法定義
 * @param <ID> 唯一識別符的類型
 */
public abstract class BaseEntity<ID> {
    public abstract ID getId();

    @Override
    public int hashCode() {
        ID id = getId();
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BaseEntity<?> other)) return false;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public String toString() {
        ID id = getId();
        return String.format("%s[id=%s]", getClass().getSimpleName(), id);
    }
}
