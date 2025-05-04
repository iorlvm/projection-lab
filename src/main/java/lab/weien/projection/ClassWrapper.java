package lab.weien.projection;

import lab.weien.projection.mapper.MapperManager;
import lab.weien.projection.mapper.ProjectionMapper;
import lab.weien.projection.resolver.DtoResolver;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public class ClassWrapper<DTO> {

    private final Class<?> projection;
    private final ProjectionMapper<DTO> mapper;

    public ClassWrapper(Class<DTO> dtoClass) {
        this.projection = DtoResolver.resolve(dtoClass);
        this.mapper = MapperManager.getMapper(projection, dtoClass);
    }

    public DTO convert(Object source) {
        if (source == null) return null;

        if (!projection.isAssignableFrom(source.getClass())) {
            throw new IllegalArgumentException("Source type is not match with DTO type");
        }

        return mapper.map(source);
    }

    public <S extends Collection<?>> List<DTO> convert(S source) {
        return source.stream()
                .map(this::convert)
                .toList();
    }
}
