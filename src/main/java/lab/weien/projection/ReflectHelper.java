package lab.weien.projection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
    public static List<Class<?>> resolveCollectionElementType(Field field) {
        if (field == null) return List.of();

        if (!Collection.class.isAssignableFrom(field.getType())) return List.of();

        return resolveGeneric(field.getGenericType());
    }

    public static List<Class<?>> resolveGeneric(Type genericType) {
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            return Stream.of(typeArguments).map(type -> {
                if (type instanceof Class<?> rawType) {
                    return rawType;
                } else {
                    // 無法解析的情況下, 暫時回退到 Object.class 避免錯誤
                    return Object.class;
                }
            }).toList();
        }
        return List.of();
    }

    public static boolean isSetter(Method method) {
        if (isInvalidSetterOrGetter(method)) return false;
        return method.getName()
                .startsWith("set") &&
                method.getParameterCount() == 1 &&
                method.getReturnType().equals(Void.TYPE);
    }

    public static boolean isGetter(Method method) {
        if (isInvalidSetterOrGetter(method)) return false;
        return method.getName()
                .startsWith("get") &&
                method.getParameterCount() == 0;
    }

    private static boolean isInvalidSetterOrGetter(Method method) {
        return method == null || method.getName().length() < 4;
    }

    public static String extractPropertyName(String methodName) {
        if (methodName == null || methodName.length() < 4) {
            throw new IllegalArgumentException("methodName is empty or less than 4 characters");
        }

        StringBuilder sb = new StringBuilder(methodName);
        sb.delete(0, 3);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }
}
