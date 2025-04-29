package lab.weien.projection;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.Map;

import static lab.weien.projection.ReflectHelper.extractPropertyName;
import static lab.weien.projection.ReflectHelper.isSetter;

@Slf4j
public class DtoResolver {
    public static Class<?> resolve(Class<?> dtoClass) {
        if (dtoClass.isInterface()) {
            return dtoClass;
        } else if (Modifier.isAbstract(dtoClass.getModifiers())) {
            throw new IllegalArgumentException("The class is abstract: " + dtoClass.getName());
        }

        if (dtoClass.isRecord()) {
            return resolveRecord(dtoClass).build();
        } else {
            return resolveNonRecord(dtoClass).build();
        }
    }

    private static ProjectionBuilder resolveRecord(Class<?> recordClass) {
        ProjectionBuilder builder = new ProjectionBuilder();
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            String name = component.getName();
            Class<?> type = component.getType();
            Class<?> genericType = resolveGeneric(component.getGenericType());

            addField(builder, name, type, genericType, null);
        }
        return builder;
    }

    private static ProjectionBuilder resolveNonRecord(Class<?> dtoClass) {
        ProjectionBuilder builder = new ProjectionBuilder();
        Method[] methods = dtoClass.getMethods();
        for (Method method : methods) {
            resolveProcess(builder, method);
        }
        return builder;
    }

    private static void resolveProcess(ProjectionBuilder builder, Method method) {
        if (!isSetter(method)) return;

        // TODO: 其他擴充...
        String valueExpression = null;

        String name = extractPropertyName(method.getName());
        Class<?> type = method.getParameterTypes()[0];
        Class<?> genericType = resolveGeneric(method.getGenericParameterTypes()[0]);

        addField(builder, name, type, genericType, valueExpression);
    }

    private static void addField(ProjectionBuilder builder, String name, Class<?> type, Class<?> genericType, String valueExpression) {
        if (type == null) throw new IllegalArgumentException("The field type is null: " + name);

        if (notResolvableBySpring(type)) {
            type = resolve(type);
        }
        if (genericType != null && notResolvableBySpring(genericType)) {
            genericType = resolve(genericType);
        }

        builder.addField(name, type, genericType, valueExpression);
    }

    private static Class<?> resolveGeneric(Type genericType) {
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> argument) {
                return argument;
            } else if (typeArguments.length > 1) {
                handleMultipleGeneric(parameterizedType.getRawType());
            }
        }
        return null;
    }

    private static void handleMultipleGeneric(Type rawType) {
        if (rawType instanceof Class<?> rawClass && Map.class.isAssignableFrom(rawClass)) {
            log.info("The generic type '{}' is a Map with multiple arguments. "
                            + "Map generics will be ignored, falling back to raw type.",
                    rawType.getTypeName());
        } else {
            log.warn("The generic type '{}' has multiple arguments but this implementation "
                            + "only supports single generic arguments. The type will be treated as raw.",
                    rawType.getTypeName());
        }
    }

    private static boolean notResolvableBySpring(Class<?> type) {
        return !type.getName().startsWith("java") &&
                !type.getName().startsWith("org.springframework");
    }
}
