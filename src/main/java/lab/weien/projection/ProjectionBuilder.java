package lab.weien.projection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProjectionBuilder {
    private final List<ProjectionFactory.Field> fields = new ArrayList<>();

    public ProjectionBuilder addField(String name, Class<?> type) {
        fields.add(new ProjectionFactory.Field(name, type));
        return this;
    }

    public ProjectionBuilder fromEntity(Class<?> entityClass, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field reflectField = entityClass.getDeclaredField(fieldName);
                fields.add(new ProjectionFactory.Field(fieldName, reflectField.getType()));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field not found in entity: " + fieldName, e);
            }
        }
        return this;
    }

    public Class<?> build() {
        return ProjectionFactory.create(fields);
    }

}
