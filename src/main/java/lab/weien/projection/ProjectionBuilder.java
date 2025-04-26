package lab.weien.projection;

import lab.weien.utils.LockManager;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ProjectionBuilder {
    private static final LockManager<String> lockManager = new LockManager<>();
    private final Set<ProjectionFactory.Field> fields = new HashSet<>();

    public ProjectionBuilder addField(String fieldName, Class<?> type) {
        replaceField(new ProjectionFactory.Field(fieldName, type, null, null));
        return this;
    }

    public ProjectionBuilder addField(String fieldName, Class<?> type, String valueExpression) {
        replaceField(new ProjectionFactory.Field(fieldName, type, null, valueExpression));
        return this;
    }

    public ProjectionBuilder addNestedField(String fieldName, Class<?> nestedType) {
        replaceField(new ProjectionFactory.Field(fieldName, null, nestedType, null));
        return this;
    }

    public ProjectionBuilder addNestedField(String fieldName, Class<?> nestedType, String valueExpression) {
        replaceField(new ProjectionFactory.Field(fieldName, List.class, nestedType, valueExpression));
        return this;
    }

    public ProjectionBuilder fromEntity(Class<?> entityClass, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field reflectField = entityClass.getDeclaredField(fieldName);
                replaceField(new ProjectionFactory.Field(fieldName, reflectField.getType(), null, null));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field not found in entity: " + fieldName, e);
            }
        }
        return this;
    }

    private void replaceField(ProjectionFactory.Field field) {
        fields.remove(field);
        fields.add(field);
    }

    public Class<?> build() {
        String hash = ProjectionManager.toHash(fields);
        Class<?> res = ProjectionManager.get(hash);
        if (res == null) {
            res = lockManager.executeWithLock(hash, () -> {
                Class<?> clazz = ProjectionManager.get(hash);
                if (clazz != null) {
                    log.debug("Projection interface already exists in cache: {}", clazz.getName());
                    return clazz;
                }

                log.info("Creating projection interface.\n{}", this);
                clazz = ProjectionFactory.create(fields.stream().toList(), hash);
                ProjectionManager.register(hash, clazz);
                log.info("Projection interface created: {}", clazz.getName());
                return clazz;
            });
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ProjectionBuilder: [\n");
        fields.stream().sorted().forEach(field ->
                stringBuilder
                        .append("\t")
                        .append(field.toString())
                        .append(",\n")
        );
        if (stringBuilder.length() == 21) {
            stringBuilder.deleteCharAt(20);
        } else {
            stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
