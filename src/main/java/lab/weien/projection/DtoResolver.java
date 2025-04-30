package lab.weien.projection;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static lab.weien.projection.ReflectHelper.getAllFields;
import static lab.weien.projection.ReflectHelper.resolveGeneric;

@Slf4j
public class DtoResolver {
    public static Class<?> resolve(Class<?>... Classes) {
        if (Classes == null || Classes.length == 0) {
            throw new IllegalArgumentException("Classes is empty");
        }

        Class<?> aClass = Classes[0];

        if (aClass.isInterface()) {
            return aClass;
        } else if (Modifier.isAbstract(aClass.getModifiers())) {
            throw new IllegalArgumentException("The class is abstract: " + aClass.getName());
        }

        Map<String, Class<?>> registry = register(Classes);

        if (aClass.isRecord()) {
            return resolveRecord(aClass, registry).build();
        } else {
            return resolveNonRecord(aClass, registry).build();
        }
    }

    private static Map<String, Class<?>> register(Class<?>... Classes) {
        Class<?> aClass = Classes[0];
        Map<String, Class<?>> registry = new HashMap<>();
        TypeVariable<? extends Class<?>>[] typeParameters = aClass.getTypeParameters();

        if (Classes.length - 1 < typeParameters.length) {
            Arrays.fill(Classes, Classes.length, typeParameters.length, Object.class);
        } else if (Classes.length - 1 > typeParameters.length) {
            throw new IllegalArgumentException("The number of type parameters is not equal to the number of actual type arguments: " + aClass.getName());
        }

        for (int i = 0; i < typeParameters.length; i++) {
            String name = typeParameters[i].getName();
            Class<?> type = Classes[i + 1];
            registry.put(name, type);
        }
        return registry;
    }

    private static ProjectionBuilder resolveRecord(Class<?> recordClass, Map<String, Class<?>> registry) {
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

    private static ProjectionBuilder resolveNonRecord(Class<?> dtoClass, Map<String, Class<?>> registry) {
        ProjectionBuilder builder = new ProjectionBuilder();
        List<Field> fields = getAllFields(dtoClass);
        for (Field field : fields) {
            resolveProcess(builder, field, registry);
        }
        return builder;
    }

    private static void resolveProcess(ProjectionBuilder builder, Field field, Map<String, Class<?>> registry) {
        // TODO: 其他擴充...
        String valueExpression = null;
        Type genericType = field.getGenericType();

        String name = field.getName();
        Class<?> type = resolveType(field, registry);
        List<Class<?>> genericTypes = resolveGeneric(genericType);

        addField(builder, name, type, genericTypes, valueExpression);
    }

    private static void addField(ProjectionBuilder builder, String name, Class<?> type, List<Class<?>> genericTypes, String valueExpression) {
        if (type == null) throw new IllegalArgumentException("The field type is null: " + name);

        if (needsResolving(type)) {
            type = resolve(type);
        }
        if (genericTypes != null && !genericTypes.isEmpty()) {
            genericTypes = genericTypes.stream()
                    .map(genericType -> needsResolving(genericType) ?
                            resolve(genericType) : genericType)
                    .toList();
        }

        builder.addField(name, type, genericTypes, valueExpression);
    }

    private static Class<?> resolveType(Field field, Map<String, Class<?>> registry) {
        Class<?> rawType = field.getType();
        Type genericType = field.getGenericType();

        switch (genericType) {
            case Class<?> clazz -> {
                return clazz;
            }
            case ParameterizedType paramType -> {
                Type[] actualTypes = paramType.getActualTypeArguments();
                Class<?>[] classes = convertToClasses(rawType, actualTypes);

                return resolve(classes);
            }
            case TypeVariable<?> typeVar -> {
                String name = typeVar.getName();
                Class<?> type = registry.get(name);

                return type != null ? type : rawType;
            }
            case GenericArrayType arrayType -> {
                Type genericComponentType = arrayType.getGenericComponentType();
                if (!(genericComponentType instanceof TypeVariable<?> typeVar)) {
                    return rawType;
                }

                String name = typeVar.getName();
                Class<?> type = registry.get(name);

                return type != null ? type : rawType;
            }
            default -> {
                return rawType;
            }
        }
    }

    private static Class<?>[] convertToClasses(Class<?> rawType, Type[] types) {
        return Stream.concat(
                Stream.of(rawType),
                Arrays.stream(types).map(type -> {
                    if (type instanceof Class<?> clazz) {
                        return clazz;
                    } else if (type instanceof WildcardType wildcardType) {
                        Type[] upperBounds = wildcardType.getUpperBounds();
                        return (upperBounds.length > 0 && upperBounds[0] instanceof Class<?> clazz) ?
                                clazz :
                                Object.class;
                    } else {
                        return Object.class;
                    }
                })
        ).toArray(Class<?>[]::new);
    }

    private static boolean needsResolving(Class<?> type) {
        return !skipResolving(type);
    }

    private static boolean skipResolving(Class<?> type) {
        return type.getName().startsWith("java") ||
                type.getName().startsWith("org.springframework");
    }
}
