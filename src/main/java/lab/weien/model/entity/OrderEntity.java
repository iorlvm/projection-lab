package lab.weien.model.entity;

import jakarta.persistence.*;
import lab.weien.model.core.Identifiable;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity implements Identifiable<String> {
    @Id
    @Column(name = "order_id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items;
}