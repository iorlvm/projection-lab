package lab.weien.projection;

import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GenericTypeRegistry {
    private static final Map<String, Map<Class<?>, TypeVariable<?>[]>> threadGenericTypesMap = new ConcurrentHashMap<>();

    public static void registerGenericInfo(String threadId, Class<?> clazz, TypeVariable<?>[] typeParameters) {
        Map<Class<?>, TypeVariable<?>[]> threadMap = threadGenericTypesMap.computeIfAbsent(threadId, k -> new ConcurrentHashMap<>());
        threadMap.put(clazz, typeParameters);
    }

    public static TypeVariable<?>[] getGenericInfo(String threadId, Class<?> clazz) {
        return Optional.ofNullable(threadGenericTypesMap.get(threadId))
                .map(map -> map.get(clazz))
                .orElse(null);
    }

    public static void clearThread(String threadId) {
        threadGenericTypesMap.remove(threadId);
    }
}