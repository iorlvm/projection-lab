package lab.weien.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lab.weien.model.core.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@IdClass(OrderItemEntity.OrderItemId.class)
@Table(name = "order_items")
public class OrderItemEntity extends BaseEntity<OrderItemEntity.OrderItemId> {
    @Id
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private OrderEntity order;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "count")
    private Integer count;

    @Override
    public OrderItemId getId() {
        if (order == null || product == null) return null;
        return new OrderItemId(order.getId(), product.getId());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemId implements Serializable {
        private String order;
        private Long product;
    }
}
