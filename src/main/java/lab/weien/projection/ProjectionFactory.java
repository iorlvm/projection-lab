package lab.weien.projection;

import lab.weien.projection.utils.LoggerWrapper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

public class ProjectionFactory {
    private static final LoggerWrapper log = new LoggerWrapper(ProjectionFactory.class, "[ProjectionFactory] ");
    public static final String DYNAMIC_CLASS_NAME_PREFIX = "lab.weien.projection.dynamic.$D";

    static Class<?> create(TypeVariable<? extends Class<?>>[] typeParameters, List<Field> fields, String hash) {
        try {
            String dynamicInterfaceName = DYNAMIC_CLASS_NAME_PREFIX + hash;

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Builder<?> builder = byteBuddy
                    .makeInterface()
                    .name(dynamicInterfaceName);

            if (typeParameters != null) {
                for (TypeVariable<? extends Class<?>> typeParam : typeParameters) {
                    builder.typeVariable(typeParam.getName());
                }
            }

            for (Field field : fields) {
                builder = builder.defineMethod(
                                "get" + capitalize(field.name()),
                                field.generic(),
                                Visibility.PUBLIC
                        )
                        .withoutCode()
                        .annotateMethod(defineValueAnnotation(field.valueExpression()));
            }

            return builder
                    .make()
                    .load(ProjectionFactory.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        } catch (Exception e) {
            Class<?> res = ProjectionManager.get(hash);
            if (res != null) {
                log.error("Lock mechanism failed! Projection interface was created by another thread unexpectedly: {}", res.getName(), e);
                return res;
            }

            throw new RuntimeException("Failed to generate projection interface", e);
        }
    }

    private static AnnotationDescription defineValueAnnotation(String valueExpression) {
        return AnnotationDescription.Builder.ofType(Value.class)
                .define("value", "#{" + valueExpression + "}")
                .build();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public record Field(String name, TypeDescription.Generic generic,
                        String valueExpression) implements Comparable<Field> {
        @Override
        public String valueExpression() {
            return valueExpression != null ? valueExpression : ("target." + name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Field other)) return false;
            return Objects.equals(this.name(), other.name());
        }

        @Override
        public int compareTo(Field other) {
            if (other == null) return 1;
            if (this == other) return 0;
            String otherStr = other.toString();
            String thisStr = this.toString();
            return thisStr.compareTo(otherStr);
        }
    }
}
