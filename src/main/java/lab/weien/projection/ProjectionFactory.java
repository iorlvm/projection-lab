package lab.weien.projection;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

public class ProjectionFactory {
    public static Class<?> create(List<Field> fields, String hash) {
        try {
            String dynamicInterfaceName = "lab.weien.dynamic.projection.$DynamicProjection" + hash;

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Builder<?> builder = byteBuddy
                    .makeInterface()
                    .name(dynamicInterfaceName);

            for (Field field : fields) {
                if (field.nestedClazz() != null) {
                    Class<?> nestedClass = field.nestedClazz();

                    TypeDescription.Generic listOfNestedClassGeneric = TypeDescription.Generic.Builder
                            .parameterizedType(List.class, nestedClass)
                            .build();

                    builder = builder.defineMethod(
                                    "get" + capitalize(field.name()),
                                    listOfNestedClassGeneric,
                                    Visibility.PUBLIC
                            )
                            .withoutCode()
                            .annotateMethod(defineValueAnnotation(field.valueExpression()));
                } else {
                    builder = builder.defineMethod(
                                    "get" + capitalize(field.name()),
                                    field.clazz(),
                                    Visibility.PUBLIC
                            )
                            .withoutCode()
                            .annotateMethod(defineValueAnnotation(field.valueExpression()));
                }
            }

            return builder
                    .make()
                    .load(ProjectionFactory.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        } catch (Exception e) {
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

    public record Field(String name, Class<?> clazz, Class<?> nestedClazz, String valueExpression) {
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
    }
}
