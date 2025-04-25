package lab.weien.projection;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectionBuilder {
    private final Set<ProjectionFactory.Field> fields = new HashSet<>();

    public ProjectionBuilder addField(String fieldName, Class<?> type) {
        fields.add(new ProjectionFactory.Field(fieldName, type, null, null));
        return this;
    }

    public ProjectionBuilder addField(String fieldName, Class<?> type, String valueExpression) {
        fields.add(new ProjectionFactory.Field(fieldName, type, null, valueExpression));
        return this;
    }

    public ProjectionBuilder addNestedField(String fieldName, Class<?> nestedType) {
        fields.add(new ProjectionFactory.Field(fieldName, null, nestedType, null));
        return this;
    }

    public ProjectionBuilder addNestedField(String fieldName, Class<?> nestedType, String valueExpression) {
        fields.add(new ProjectionFactory.Field(fieldName, List.class, nestedType, valueExpression));
        return this;
    }

    public ProjectionBuilder fromEntity(Class<?> entityClass, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field reflectField = entityClass.getDeclaredField(fieldName);
                fields.add(new ProjectionFactory.Field(fieldName, reflectField.getType(), null, null));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field not found in entity: " + fieldName, e);
            }
        }
        return this;
    }

    public Class<?> build() {
        // TODO: 未來管理器擴充嫁接
        return ProjectionFactory.create(fields.stream().toList(), hashCode() + "");
    }
}
