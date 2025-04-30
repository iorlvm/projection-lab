package lab.weien.projection;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.TypeVariable;
import java.util.List;

import static lab.weien.projection.ReflectHelper.getAllFields;
import static lab.weien.projection.ReflectHelper.resolveGeneric;

@Slf4j
public class DtoResolver {
    public static Class<?> resolve(Class<?> dtoClass) {
        String threadId = getThreadId();
        try {
            return resolve(threadId, dtoClass);
        } finally {
            GenericTypeRegistry.clearThread(threadId);
        }
    }

    private static Class<?> resolve(String threadId, Class<?> dtoClass) {
        if (dtoClass.isInterface()) {
            return dtoClass;
        } else if (Modifier.isAbstract(dtoClass.getModifiers())) {
            throw new IllegalArgumentException("The class is abstract: " + dtoClass.getName());
        }

        registerGenericInfo(dtoClass, threadId);

        // TODO - 動態類型的泛型支援
        //  1. 當前問題：
        //    - 泛型參數無法被正確保留 (例如：ClassName<T> 會丟失 T 的資訊)
        //    - 生成的動態類型沒有包含原始類型的泛型資訊
        //  2. 需要支援的場景：
        //    - ClassName<T, R, Q> 類型的泛型參數
        //  3. 相關問題：見 ProjectionFactory 中的泛型解析問題
        //  4. 影響範圍：
        //    - Record 類型
        //    - 一般類型;
        if (dtoClass.isRecord()) {
            return resolveRecord(threadId, dtoClass).build();
        } else {
            return resolveNonRecord(threadId, dtoClass).build();
        }
    }

    private static ProjectionBuilder resolveRecord(String threadId, Class<?> recordClass) {
        ProjectionBuilder builder = new ProjectionBuilder();
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            String name = component.getName();
            Class<?> type = component.getType();
            List<Class<?>> genericTypes = resolveGeneric(component.getGenericType());

            addField(threadId, builder, name, type, genericTypes, null);
        }
        return builder;
    }

    private static ProjectionBuilder resolveNonRecord(String threadId, Class<?> dtoClass) {
        ProjectionBuilder builder = new ProjectionBuilder();
        List<Field> fields = getAllFields(dtoClass);
        for (Field field : fields) {
            resolveProcess(threadId, builder, field);
        }
        return builder;
    }

    private static void resolveProcess(String threadId, ProjectionBuilder builder, Field field) {
        // TODO: 其他擴充...
        String valueExpression = null;

        String name = field.getName();
        Class<?> type = field.getType();
        List<Class<?>> genericTypes = resolveGeneric(field.getGenericType());

        addField(threadId, builder, name, type, genericTypes, valueExpression);
    }

    private static void addField(String threadId, ProjectionBuilder builder, String name, Class<?> type, List<Class<?>> genericTypes, String valueExpression) {
        if (type == null) throw new IllegalArgumentException("The field type is null: " + name);

        if (needsResolving(type)) {
            type = resolve(threadId, type);
        }
        if (genericTypes != null && !genericTypes.isEmpty()) {
            genericTypes = genericTypes.stream()
                    .map(genericType -> needsResolving(genericType) ?
                            resolve(threadId, genericType) : genericType)
                    .toList();
        }

        builder.addField(name, type, genericTypes, valueExpression);
    }

    private static void registerGenericInfo(Class<?> clazz, String threadId) {
        if (skipResolving(clazz)) return;

        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        if (typeParameters.length == 0) return;

        GenericTypeRegistry.registerGenericInfo(threadId, clazz, typeParameters);
    }

    private static boolean needsResolving(Class<?> type) {
        return !skipResolving(type);
    }

    private static boolean skipResolving(Class<?> type) {
        return type.getName().startsWith("java") ||
                type.getName().startsWith("org.springframework");
    }

    public static String getThreadId() {
        return String.valueOf(Thread.currentThread().threadId());
    }
}
