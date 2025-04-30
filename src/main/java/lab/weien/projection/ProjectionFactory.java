package lab.weien.projection;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ProjectionFactory {
    public static final String DYNAMIC_CLASS_NAME_PREFIX = "lab.weien.projection.dynamic.$D";

    static Class<?> create(List<Field> fields, String hash) {
        try {
            String dynamicInterfaceName = DYNAMIC_CLASS_NAME_PREFIX + hash;

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Builder<?> builder = byteBuddy
                    .makeInterface()
                    .name(dynamicInterfaceName);

            for (Field field : fields) {
                if (field.genericTypes() != null && !field.genericTypes().isEmpty()) {
                    log.info("Field[name={}, type={}, genericTypes={}]", field.name(), field.type(), field.genericTypes());
                    TypeDescription.Generic fieldGenericType = TypeDescription.Generic.Builder
                            .parameterizedType(field.type(), field.genericTypes())
                            .build();

                    builder = builder.defineMethod(
                                    "get" + capitalize(field.name()),
                                    fieldGenericType,
                                    Visibility.PUBLIC
                            )
                            .withoutCode()
                            .annotateMethod(defineValueAnnotation(field.valueExpression()));
                } else {
                    builder = builder.defineMethod(
                                    "get" + capitalize(field.name()),
                                    field.type(),
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

    public record Field(String name, Class<?> type, List<Class<?>> genericTypes,
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
