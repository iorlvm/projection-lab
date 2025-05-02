package lab.weien.projection.impl;

import lab.weien.projection.DtoResolver;
import lab.weien.projection.ProjectionBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ResolverImpl implements DtoResolver.Resolver {
    private static final Map<Class<?>, Class<?>> CACHE = new ConcurrentHashMap<>();

    @Setter
    private boolean safeMode = false;

    @Override
    public Class<?> resolve(Class<?> clazz) {
        if (skipResolving(clazz)) return clazz;

        Class<?> cached = CACHE.get(clazz);
        if (cached != null) return cached;

        List<FieldContainer> fields = getAllField(clazz);
        if (fields.isEmpty()) return clazz;

        ProjectionBuilder builder = new ProjectionBuilder(clazz);
        for (FieldContainer field : fields) {
            builder.addField(field.fieldName(), field.genericType(), field.valueExpression());
        }
        Class<?> result = builder.build();

        CACHE.put(clazz, result);
        return result;
    }

    private List<FieldContainer> getAllField(Class<?> clazz) {
        return getAllField(clazz, Map.of());
    }

    private List<FieldContainer> getAllField(Class<?> clazz, Map<String, Type> typeMap) {
        Field[] fields = clazz.getDeclaredFields();
        List<FieldContainer> res = new ArrayList<>(fields.length * 2);

        for (Field field : fields) {
            if (skipField(clazz, field)) continue;

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

        // TODO: 其他擴充
        //  - 掃描欄位 @Value("#{valueExpression}"), 取得 valueExpression
        String valueExpression = null;

        return new FieldContainer(fieldName, genericType, valueExpression);
    }

    private Type resolveType(Type genericType, Map<String, Type> typeMap) {
        switch (genericType) {
            case Class<?> clazz: {
                return resolve(clazz);
            }
            case ParameterizedType paramType: {
                Class<?> resolved = resolve((Class<?>) paramType.getRawType());
                Type[] copied = Arrays.copyOf(paramType.getActualTypeArguments(), paramType.getActualTypeArguments().length);
                for (int i = 0; i < copied.length; i++) {
                    copied[i] = resolveType(copied[i], typeMap);
                }

                return new ParamTypeContainer(resolved, copied);
            }
            case TypeVariable<?> typeVar: {
                String name = typeVar.getName();
                Type type = typeMap.get(name);
                if (type != null) {
                    // 進入這裡唯一的可能性只有往父類搜尋欄位繼承到的欄位
                    return resolveType(type, typeMap);
                } else {
                    // TODO:
                    GenericDeclaration genericDeclaration = typeVar.getGenericDeclaration();

                    return new TypeVariableContainer(name, genericDeclaration);
                }
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

    // util methods
    private boolean skipField(Class<?> clazz, Field field) {
        // TODO: 未來擴充
        //  - 掃描欄位有無 @ProjectionIgnore 標示, 有的話跳過 (true)

        if (!safeMode) return false;

        String fieldName = field.getName();
        return missingGetterOrSetter(clazz, fieldName);
    }

    private boolean missingGetterOrSetter(Class<?> clazz, String fieldName) {
        try {
            clazz.getMethod(setterName(fieldName));
            clazz.getMethod(getterName(fieldName));
            return false;
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    private String setterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private String getterName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private boolean needsResolving(Class<?> clazz) {
        return !skipResolving(clazz);
    }

    private boolean skipResolving(Class<?> clazz) {
        return clazz.isInterface() ||
                clazz.getName().startsWith("java") ||
                clazz.getName().startsWith("org.springframework");
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

    private record TypeVariableContainer(String name, GenericDeclaration genericDeclaration) implements TypeVariable<GenericDeclaration> {
        @Override
        public GenericDeclaration getGenericDeclaration() {
            return genericDeclaration;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Type[] getBounds() {
            return new Type[]{Object.class};
        }

        @Override
        public AnnotatedType[] getAnnotatedBounds() {
            return new AnnotatedType[0];
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
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
}
