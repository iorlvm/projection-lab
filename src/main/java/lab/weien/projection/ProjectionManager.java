package lab.weien.projection;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectionManager {
    private static final ConcurrentHashMap<String, Class<?>> projectionCache = new ConcurrentHashMap<>();

    static void register(String key, Class<?> projectionClass) {
        projectionCache.put(key, projectionClass);
    }

    static Class<?> get(String key) {
        return projectionCache.get(key);
    }

    static String toHash(Set<ProjectionFactory.Field> fields) {
        byte[] bytes = fieldsToString(fields).getBytes();
        return UUID.nameUUIDFromBytes(bytes).toString().replace("-", "");
    }

    private static String fieldsToString(Set<ProjectionFactory.Field> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("fields is empty");
        }

        StringBuilder stringBuilder = new StringBuilder();
        fields.stream().sorted().forEach(field -> stringBuilder.append(field.toString()));
        return stringBuilder.toString();
    }
}
