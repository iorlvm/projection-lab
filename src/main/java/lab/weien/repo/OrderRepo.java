package lab.weien.repo;

import lab.weien.model.core.Identifiable;
import lab.weien.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepo extends JpaRepository<OrderEntity, String> {
    /**
     * 配合定義 JPQL查詢 可以避免 N+1 次查詢
     * {@code @Query("SELECT ... LEFT JOIN ...")}
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product")
    <T extends Identifiable<String>> List<T> findAllBy(Class<T> type);
}
