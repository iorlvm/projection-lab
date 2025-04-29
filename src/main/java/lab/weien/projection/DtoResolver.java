package lab.weien.projection;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.List;

import static lab.weien.projection.ReflectHelper.*;

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
            List<Class<?>> genericTypes = resolveGeneric(component.getGenericType());

            addField(builder, name, type, genericTypes, null);
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
        List<Class<?>> genericTypes = resolveGeneric(method.getGenericParameterTypes()[0]);

        addField(builder, name, type, genericTypes, valueExpression);
    }

    private static void addField(ProjectionBuilder builder, String name, Class<?> type, List<Class<?>> genericTypes, String valueExpression) {
        if (type == null) throw new IllegalArgumentException("The field type is null: " + name);

        if (notResolvableBySpring(type)) {
            type = resolve(type);
        }
        if (genericTypes != null && !genericTypes.isEmpty()) {
            genericTypes = genericTypes.stream()
                    .map(genericType -> notResolvableBySpring(genericType) ? resolve(genericType) : genericType)
                    .toList();
        }

        builder.addField(name, type, genericTypes, valueExpression);
    }

    private static boolean notResolvableBySpring(Class<?> type) {
        return !type.getName().startsWith("java") &&
                !type.getName().startsWith("org.springframework");
    }
}
