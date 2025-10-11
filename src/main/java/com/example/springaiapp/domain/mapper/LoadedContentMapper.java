package com.example.springaiapp.domain.mapper;

import com.example.springaiapp.domain.model.LoadedContentModel;
import com.example.springaiapp.infrastracture.entity.LoadedContentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct маппер для преобразования между LoadedContentEntity и LoadedContentModel
 * Автоматически генерирует реализацию маппинга
 */
@Mapper(componentModel = "spring")
public interface LoadedContentMapper {
    
    LoadedContentMapper INSTANCE = Mappers.getMapper(LoadedContentMapper.class);
    
    /**
     * Преобразование Entity в Model
     * @param entity сущность загруженного контента
     * @return модель загруженного контента
     */
    LoadedContentModel entityToModel(LoadedContentEntity entity);
    
    /**
     * Преобразование Model в Entity
     * @param model модель загруженного контента
     * @return сущность загруженного контента
     */
    @Mapping(target = "loadedAt", expression = "java(model.getLoadedAt() != null ? model.getLoadedAt() : java.time.LocalDateTime.now())")
    LoadedContentEntity modelToEntity(LoadedContentModel model);
    
}
