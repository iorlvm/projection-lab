package lab.weien.controller;

import lab.weien.model.core.Identifiable;
import lab.weien.model.projection.OrderLite;
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
    public List<? extends Identifiable<String>> test() {
        // return orderRepo.findAllBy(OrderEntity.class);
        return orderRepo.findAllBy(OrderLite.class);
        // return orderRepo.findAllBy(OrderLiteUseValue.class);
    }
}
