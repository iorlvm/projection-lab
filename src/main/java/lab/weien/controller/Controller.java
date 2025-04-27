package lab.weien.controller;

import lab.weien.model.entity.OrderEntity;
import lab.weien.model.entity.OrderItemEntity;
import lab.weien.projection.ProjectionBuilder;
import lab.weien.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class Controller {
    @Autowired
    private OrderRepo orderRepo;

    @GetMapping("/test")
    public List<?> test() {
        Class<?> itemProjection = new ProjectionBuilder()
                .fromEntity(OrderItemEntity.class, "count")
                .addField("name", String.class, "target.product.name")
                .addField("price", Long.class, "target.product.price")
                .addField("totalPrice", Long.class, "target.product.price * target.count")
                .build();
        Class<?> orderProjection = new ProjectionBuilder()
                .fromEntity(OrderEntity.class, "id", "userId")
                .addField("orderList", List.class, itemProjection, "target.items")
                .build();
        return orderRepo.findAllBy(orderProjection);

        // return orderRepo.findAllBy(OrderEntity.class);
        // return orderRepo.findAllBy(OrderLite.class);
        // return orderRepo.findAllBy(OrderLiteUseValue.class);
    }
}
