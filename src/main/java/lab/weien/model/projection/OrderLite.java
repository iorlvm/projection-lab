package lab.weien.model.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * JPA Projection 寫法比較
 * 寫法一：直觀的介面寫法
 * - 使用內部介面定義關聯物件的結構
 * - 透過 default method 來獲取關聯物件的屬性
 * - 優點：
 *   - 結構清晰，易於理解
 *   - 可以在 default method 中加入複雜的邏輯處理
 *   - 使用 @JsonIgnore 可以靈活控制序列化
 * - 缺點：
 *   - 需要額外定義多個介面（如 ProductLite）
 *   - 程式碼較為冗長
 */
public interface OrderLite {
    String getId();
    String getUserId();
    List<OrderItemLite> getItems();

    interface OrderItemLite {
        Integer getCount();

        @JsonIgnore
        ProductLite getProduct();

        default String getName() {
            return getProduct().getName();
        }

        default Long getPrice() {
            return getProduct().getPrice();
        }

        default Long getTotalPrice() {
            return getCount() * getPrice();
        }
    }

    interface ProductLite {
        String getName();
        Long getPrice();
    }
}
