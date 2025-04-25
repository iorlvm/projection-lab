package lab.weien.model.entity;

import jakarta.persistence.*;
import lab.weien.model.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity<String> {
    @Id
    @Column(name = "order_id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items;
}