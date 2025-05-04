package lab.weien.model.dto;

import lab.weien.projection.annotation.ProjectionDto;
import lab.weien.projection.annotation.ProjectionIgnore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Data
@ProjectionDto
public class OrderDto {
    private String id;

    @ProjectionIgnore
    private String userId;

    private List<OrderDto.OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        @Value("#{target.product.name}")
        private String name;

        @Value("#{target.product.price}")
        private Long price;

        @Value("#{target.count * target.product.price}")
        private Long totalPrice;
    }
}
