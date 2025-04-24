package lab.weien.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@IdClass(OrderItemEntity.OrderItemId.class)
@Table(name = "order_items")
public class OrderItemEntity {
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemId implements Serializable {
        private String order;
        private Long product;
    }
}
