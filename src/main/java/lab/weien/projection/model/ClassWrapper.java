package lab.weien.projection.model;

import lab.weien.projection.utils.DtoResolver;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

public class ClassWrapper<DTO> {
    private final Class<DTO> dtoClass;
    @Getter
    private final Class<?> projection;

    public ClassWrapper(Class<DTO> dtoClass) {
        this.dtoClass = dtoClass;
        this.projection = DtoResolver.resolve(dtoClass);
    }

    public DTO convert(Object source) {
        if (source == null) return null;
        // TODO:
        return null;
    }

    public <S extends Collection<?>> List<DTO> convert(S source) {
        return source.stream()
                .map(this::convert)
                .toList();
    }
}
