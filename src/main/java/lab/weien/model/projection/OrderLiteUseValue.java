package lab.weien.model.projection;

import lab.weien.model.core.Identifiable;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * JPA Projection 寫法比較
 * 寫法二：使用 @Value 注解的投影寫法
 * - 使用 Spring 的 @Value 注解直接獲取關聯物件的屬性
 * - 語法：{@code @Value("#{target.attribute}")}
 *   - target：代表當前實體物件
 *   - attribute：要獲取的屬性路徑
 *
 * EX:
 *  {@code @Value("#{target.product.name}")}
 * - target 指向當前的 OrderItem 實體
 * - product 是 OrderItem 中的關聯對象
 * - name 是 product 對象的屬性
 *
 * 優點：
 * - 程式碼簡潔，不需要定義額外的介面
 * - 可以在注解中使用 SpEL 表達式進行運算
 * - 直接指定要獲取的屬性
 */
public interface OrderLiteUseValue extends Identifiable<String> {
    String getId();
    String getUserId();
    List<OrderItemLite> getItems();

    interface OrderItemLite {
        Integer getCount();

        @Value("#{target.product.name}")
        String getName();

        @Value("#{target.product.price}")
        Long getPrice();

        @Value("#{target.count * target.product.price}")
        Long getTotalPrice();
    }
}
