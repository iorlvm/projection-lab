package lab.weien.projection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ReflectHelper {
    public static List<Class<?>> resolveCollectionElementType(Field field) {
        if (field == null) return List.of();

        if (!Collection.class.isAssignableFrom(field.getType())) return List.of();

        return resolveGeneric(field.getGenericType());
    }

    public static List<Class<?>> resolveGeneric(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return List.of();
        }

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

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            fields.addAll(getAllFields(superclass));
        }

        return fields;
    }
}
