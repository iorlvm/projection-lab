package lab.weien.projection;

import lab.weien.projection.utils.LockManager;
import lab.weien.projection.utils.LoggerWrapper;
import net.bytebuddy.description.type.TypeDescription;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectionBuilder {
    private static final LoggerWrapper log = new LoggerWrapper(ProjectionBuilder.class, "[ProjectionBuilder] ");

    private static final String VALUE_EXPRESSION_PATTERN = "^(target(\\.[a-zA-Z][a-zA-Z0-9_]*)+)(\\s*([+\\-*/])\\s*target(\\.[a-zA-Z][a-zA-Z0-9_]*)+)*$";
    private static final LockManager<String> lockManager = new LockManager<>();

    private final Set<ProjectionFactory.Field> fields = new HashSet<>();
    private final TypeVariable<? extends Class<?>>[] typeParameters;

    public ProjectionBuilder() {
        this.typeParameters = null;
    }

    public ProjectionBuilder(Class<?> clazz) {
        // TODO: 目前使用其他方法繞過類別的泛形變數
        //  實質上對於此程式用途而言建構出泛形介面也非必要, 未來有時間再做研究
        this.typeParameters = clazz.getTypeParameters();
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ProjectionBuilder addField(String fieldName, Type type) {
        return addField(fieldName, type, null);
    }

    public ProjectionBuilder addField(String fieldName, Type type, String valueExpression) {
        validField(fieldName, type);
        valueExpression = validValueExpression(valueExpression);

        TypeDescription.Generic generic = TypeDescription.Generic.Builder.of(type).build();

        replaceField(new ProjectionFactory.Field(fieldName.trim(), generic, valueExpression));
        return this;
    }

    public ProjectionBuilder addField(String fieldName, Class<?> rawType, List<? extends Type> paramTypes) {
        return addField(fieldName, rawType, paramTypes, null);
    }

    public ProjectionBuilder addField(String fieldName, Class<?> rawType, List<? extends Type> paramTypes, String valueExpression) {
        validField(fieldName, rawType);
        valueExpression = validValueExpression(valueExpression);

        TypeDescription.Generic generic = TypeDescription.Generic.Builder
                .parameterizedType(rawType, paramTypes)
                .build();

        replaceField(new ProjectionFactory.Field(fieldName.trim(), generic, valueExpression));
        return this;
    }

    public ProjectionBuilder fromEntity(Class<?> entityClass, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field reflectField = entityClass.getDeclaredField(fieldName);
                Type type = reflectField.getGenericType();
                TypeDescription.Generic generic = TypeDescription.Generic.Builder.of(type).build();
                replaceField(new ProjectionFactory.Field(fieldName, generic, null));
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
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("ProjectionBuilder: No fields defined. Please add fields before calling build() method.");
        }

        String hash = ProjectionManager.toHash(this);
        Class<?> res = ProjectionManager.get(hash);
        if (res == null) {
            res = lockManager.executeWithLock(hash, () -> {
                Class<?> clazz = ProjectionManager.get(hash);
                if (clazz != null) {
                    log.debug("Projection interface already exists in cache: {}", clazz.getName());
                    return clazz;
                }

                log.info("Creating projection interface.\n{}", this);
                clazz = ProjectionFactory.create(typeParameters, fields.stream().toList(), hash);
                ProjectionManager.register(hash, clazz);
                log.info("Projection interface created: {}", clazz.getName());
                return clazz;
            });
        }
        return res;
    }

    public boolean contains(String fieldName) {
        ProjectionFactory.Field field = new ProjectionFactory.Field(fieldName, null, null);
        return fields.contains(field);
    }

    @Override
    public String toString() {
        if (fields.isEmpty()) {
            return "ProjectionBuilder: [ No fields defined ]";
        }

        StringBuilder stringBuilder = new StringBuilder("ProjectionBuilder: \n");
        if (typeParameters != null && typeParameters.length > 0) {
            stringBuilder.append("[ Type Parameters: ");
            for (TypeVariable<? extends Class<?>> typeParameter : typeParameters) {
                stringBuilder.append(typeParameter.getName())
                        .append(" ");
            }
            stringBuilder.append("]\nFields: ");
        }
        stringBuilder.append("[\n");
        fields.stream().sorted().forEach(field ->
                stringBuilder
                        .append("\t")
                        .append(field.toString())
                        .append(",\n")
        );
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private static void validField(String fieldName, Type type) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException("fieldName is empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("generic is null");
        }
    }

    private static String validValueExpression(String valueExpression) {
        if (valueExpression == null) return null;

        String trimmed = valueExpression.trim();
        if (!trimmed.matches(VALUE_EXPRESSION_PATTERN)) {
            throw new IllegalArgumentException("Invalid valueExpression: " + valueExpression);
        }
        return trimmed;
    }
}
