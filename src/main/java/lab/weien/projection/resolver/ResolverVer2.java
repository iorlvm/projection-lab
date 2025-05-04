package lab.weien.projection.resolver;

import lab.weien.projection.ProjectionBuilder;
import lab.weien.projection.utils.LockManager;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static lab.weien.projection.resolver.ReflectHelper.*;

@Slf4j
public class ResolverVer2 implements DtoResolver.Resolver {
    private static final Map<TypesKey, Class<?>> CACHE = new ConcurrentHashMap<>();
    private static final LockManager<TypesKey> lockManager = new LockManager<>();

    @Setter
    private boolean safeMode = false;

    @Override
    public Class<?> doResolve(Type... types) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("types must not be null or empty");
        }
        if (!(types[0] instanceof Class<?> clazz)) {
            throw new IllegalArgumentException("types[0] must be a class");
        }

        if (skipResolving(clazz)) return clazz;

        TypesKey key = new TypesKey(types);
        Class<?> cached = CACHE.get(key);
        if (cached != null) return cached;

        return lockManager.executeWithLock(key, () -> {
            Class<?> result = CACHE.get(key);
            if (result != null) return result;

            Map<String, Type> typeMap = new HashMap<>();
            TypeVariable<? extends Class<?>>[] typeParam = clazz.getTypeParameters();
            for (int i = 0; i < typeParam.length; i++) {
                Type type = (i + 1) < types.length ? types[i + 1] : Object.class;
                typeMap.put(typeParam[i].getName(), type);
            }

            List<FieldContainer> fields = getAllField(clazz, typeMap);
            if (fields.isEmpty()) return clazz;

            ProjectionBuilder builder = new ProjectionBuilder();
            for (FieldContainer field : fields) {
                builder.addField(field.fieldName(), field.genericType(), field.valueExpression());
            }
            result = builder.build();

            CACHE.put(key, result);
            return result;
        });
    }

    private List<FieldContainer> getAllField(Class<?> clazz, Map<String, Type> typeMap) {
        Field[] fields = clazz.getDeclaredFields();
        List<FieldContainer> res = new ArrayList<>(fields.length * 2);

        for (Field field : fields) {
            if (skipField(clazz, field, safeMode)) continue;

            FieldContainer resolved = resolveField(field, typeMap);
            res.add(resolved);
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            HashMap<String, Type> parentTypeMap = new HashMap<>();

            Type genericSuperclass = clazz.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                TypeVariable<?>[] typeParameters = superclass.getTypeParameters();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                for (int i = 0; i < typeParameters.length; i++) {
                    parentTypeMap.put(typeParameters[i].getName(), actualTypeArguments[i]);
                }
            }

            res.addAll(getAllField(superclass, parentTypeMap));
        }

        return res;
    }

    private FieldContainer resolveField(Field field, Map<String, Type> typeMap) {
        String fieldName = field.getName();
        Type genericType = resolveType(field.getGenericType(), typeMap);
        String valueExpression = getFieldValueExpression(field);

        return new FieldContainer(fieldName, genericType, valueExpression);
    }

    private Type resolveType(Type genericType, Map<String, Type> typeMap) {
        switch (genericType) {
            case Class<?> clazz when clazz.isArray(): {
                Class<?> componentType = clazz.getComponentType();
                Type resolvedComponentType = resolveType(componentType, typeMap);
                return Array.newInstance(
                        resolvedComponentType instanceof Class<?> ?
                                (Class<?>) resolvedComponentType :
                                Object.class,
                        0).getClass();
            }
            case Class<?> clazz: {
                return doResolve(clazz);
            }
            case ParameterizedType paramType: {
                Type[] copied = Arrays.copyOf(paramType.getActualTypeArguments(), paramType.getActualTypeArguments().length);
                for (int i = 0; i < copied.length; i++) {
                    copied[i] = resolveType(copied[i], typeMap);
                }

                Type rawType = paramType.getRawType();
                Class<?> resolved = doResolve(Stream.concat(
                        Stream.of(rawType),
                        Stream.of(copied)
                ).toArray(Type[]::new));

                return new ParamTypeContainer(resolved, copied);
            }
            case TypeVariable<?> typeVar: {
                String name = typeVar.getName();
                Type type = typeMap.get(name);
                return type != null ? resolveType(type, typeMap) : Object.class;
            }
            case GenericArrayType arrayType: {
                Type genericComponentType = arrayType.getGenericComponentType();
                Type resolved = resolveType(genericComponentType, typeMap);

                return new GenericArrayTypeContainer(resolved);
            }
            default: {
                return Object.class;
            }
        }
    }

    private record FieldContainer(String fieldName, Type genericType, String valueExpression) {
        @Override
        public int hashCode() {
            return fieldName != null ? fieldName.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FieldContainer other)) return false;
            return Objects.equals(this.fieldName(), other.fieldName());
        }
    }

    private record GenericArrayTypeContainer(Type genericComponentType) implements GenericArrayType {
        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }
    }

    private record ParamTypeContainer(Type rawType, Type[] actualTypeArguments) implements ParameterizedType {
        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments == null ? new Type[0] : actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    private record TypesKey(Type[] types) {
        @Override
        public int hashCode() {
            return Arrays.hashCode(types);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TypesKey(Type[] otherTypes))) return false;
            return Arrays.equals(this.types, otherTypes);
        }
    }
}
