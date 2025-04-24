package lab.weien.model.entity;

import jakarta.persistence.*;
import lab.weien.model.core.Identifiable;
import lombok.Data;

@Data
@Entity
@Table(name = "products")
public class ProductEntity implements Identifiable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Long price;
}
