package lab.weien.controller;

import lab.weien.model.dto.OrderDto;
import lab.weien.projection.ClassWrapper;
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
        ClassWrapper<OrderDto> wrapper = new ClassWrapper<>(OrderDto.class);

        List<?> allBy = orderRepo.findAllBy(wrapper.getProjection());

        List<OrderDto> convert = wrapper.convert(allBy);

        convert.forEach(orderDto -> {
            // 不會被 @JsonIgnore 影響
            System.out.println(orderDto.getUserId());
        });

        return convert;
    }
}
