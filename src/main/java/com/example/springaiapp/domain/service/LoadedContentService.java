package com.example.springaiapp.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.util.Pair;

import com.example.springaiapp.domain.model.LoadedContentModel;

/**
 * Интерфейс сервиса для работы с загруженным контентом
 * Содержит бизнес-логику для управления загруженным контентом
 */
public interface LoadedContentService {
    
    /**
     * Добавление списка загруженного контента
     * @param loadedContentList список контента для добавления
     * @return список добавленного контента
     */
    List<LoadedContentModel> addLoadedContentList(List<LoadedContentModel> loadedContentList);
    

    /**
     * Получение загруженного контента по хешу и имени файла
     * @param hashAndFileNames множество хешей и имен файлов
     * @return список загруженного контента
     */
    List<LoadedContentModel> getLoadedContentByHashAndFilename(Set<Pair<String, String>> hashAndFileNames);

    /**
     * Удаление загруженного контента по ID
     * @param id идентификатор контента
     * @return true если контент был удален, false если не найден
     */
    boolean deleteLoadedContentById(Long id);
    
    /**
     * Получение загруженного контента по ID
     * @param id идентификатор контента
     * @return контент или пустой Optional
     */
    Optional<LoadedContentModel> getLoadedContentById(Long id);

}
