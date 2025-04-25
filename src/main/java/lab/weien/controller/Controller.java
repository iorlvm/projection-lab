package lab.weien.controller;

import lab.weien.factory.ProjectionFactory;
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
        List<ProjectionFactory.Field> fields = List.of(
                new ProjectionFactory.Field("id", String.class),
                new ProjectionFactory.Field("userId", String.class),
                new ProjectionFactory.Field("items", List.class)
        );

        Class<?> aClass = ProjectionFactory.create(fields);
        return orderRepo.findAllBy(aClass);

        // return orderRepo.findAllBy(OrderEntity.class);
        // return orderRepo.findAllBy(OrderLite.class);
        // return orderRepo.findAllBy(OrderLiteUseValue.class);
    }
}
