package lab.weien.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class ReflectHelper {
    /**
     * 獲取指定 {@link Field} 的集合元素類型。
     *
     * 該方法會檢查字段是否為集合類型（如 List、Set 等），
     * 並嘗試提取集合中所指定的泛型參數類型。如果字段不是集合類型
     * 或未定義泛型參數，則返回 {@code null}。
     *
     * @param field 要檢查的字段，不能為 {@code null}
     * @return 如果字段為集合類型，返回其元素類型；否則返回 {@code null}
     */
    public static Class<?> resolveCollectionElementType(Field field) {
        if (field == null) return null;

        if (!Collection.class.isAssignableFrom(field.getType())) return null;

        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) return null;

        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        if (!(actualTypeArgument instanceof Class<?>)) return null;

        return (Class<?>) actualTypeArgument;
    }
}
