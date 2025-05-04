package lab.weien.projection.resolver;

import jakarta.persistence.Entity;
import lab.weien.projection.annotation.ProjectionIgnore;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;

public class ReflectHelper {
    public static boolean skipField(Class<?> clazz, Field field, boolean safeMode) {
        if (field.isAnnotationPresent(ProjectionIgnore.class)) {
            return true;
        }

        if (!safeMode) return false;
        return missingGetterOrSetter(clazz, field);
    }

    public static boolean missingGetterOrSetter(Class<?> clazz, Field field) {
        try {
            clazz.getMethod(setterName(field.getName()), field.getType());
            clazz.getMethod(getterName(field.getName()));
            return false;
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    public static String setterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String getterName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static boolean skipResolving(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Entity.class)) {
            return true;
        }

        return clazz.isInterface() ||
                clazz.getName().startsWith("java") ||
                clazz.getName().startsWith("org.springframework");
    }

    public static String getFieldValueExpression(Field field) {
        if (!field.isAnnotationPresent(Value.class)) return null;

        Value fieldAnnotation = field.getAnnotation(Value.class);
        String value = fieldAnnotation != null ? fieldAnnotation.value().trim() : "";

        if (value.startsWith("#{") && value.endsWith("}")) {
            return value.substring(2, value.length() - 1);
        } else {
            return null;
        }
    }
}
