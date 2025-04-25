package lab.weien.controller;

import lab.weien.model.entity.OrderEntity;
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
        Class<?> aClass = new ProjectionBuilder()
                .fromEntity(OrderEntity.class, "id", "userId", "items")
                .build();
        return orderRepo.findAllBy(aClass);

        // return orderRepo.findAllBy(OrderEntity.class);
        // return orderRepo.findAllBy(OrderLite.class);
        // return orderRepo.findAllBy(OrderLiteUseValue.class);
    }
}
