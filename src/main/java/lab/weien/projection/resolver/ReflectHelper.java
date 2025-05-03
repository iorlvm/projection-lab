package lab.weien.projection.resolver;

import java.lang.reflect.Field;

public class ReflectHelper {
    public static boolean skipField(Class<?> clazz, Field field, boolean safeMode) {
        // TODO: 未來擴充
        //  - 掃描欄位有無 @ProjectionIgnore 標示, 有的話跳過 (true)

        if (!safeMode) return false;

        String fieldName = field.getName();
        return missingGetterOrSetter(clazz, fieldName);
    }

    public static boolean missingGetterOrSetter(Class<?> clazz, String fieldName) {
        try {
            clazz.getMethod(setterName(fieldName));
            clazz.getMethod(getterName(fieldName));
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
        // TODO: 未來擴充
        //  - 掃描類別有無 @Entity 標示, 有的話跳過 (true)

        return clazz.isInterface() ||
                clazz.getName().startsWith("java") ||
                clazz.getName().startsWith("org.springframework");
    }

    public static String getFieldValueExpression(Field field) {
        // TODO: 其他擴充
        //  - 掃描欄位 @Value("#{valueExpression}"), 取得 valueExpression
        return null;
    }
}
