package lab.weien.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDto {
    private String id;
    private String userId;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Integer count;
        private ProductDto product;
    }

    @Data
    public static class ProductDto {
        private String name;
        private Long price;
    }
}
