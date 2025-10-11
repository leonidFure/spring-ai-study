package com.example.springaiapp.domain.service.impl;

import com.example.springaiapp.domain.mapper.LoadedContentMapper;
import com.example.springaiapp.domain.model.LoadedContentModel;
import com.example.springaiapp.domain.service.LoadedContentService;
import com.example.springaiapp.infrastracture.repository.LoadedContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с загруженным контентом
 * Содержит бизнес-логику для управления загруженным контентом
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoadedContentServiceImpl implements LoadedContentService {
    
    private final LoadedContentRepository loadedContentRepository;
    private final LoadedContentMapper loadedContentMapper;
    
    @Override
    @Transactional
    public List<LoadedContentModel> addLoadedContentList(final List<LoadedContentModel> loadedContentList) {
        log.info("Добавление списка загруженного контента, количество элементов: {}", loadedContentList.size());
        
        final var entities = loadedContentList.stream()
                .map(loadedContentMapper::modelToEntity)
                .collect(Collectors.toList());
        
        final var savedEntities = loadedContentRepository.saveAll(entities);
        
        log.info("Успешно добавлено {} элементов загруженного контента", savedEntities.size());
        
        return savedEntities.stream()
                .map(loadedContentMapper::entityToModel)
                .collect(Collectors.toList());
    }

    
    @Override
    public List<LoadedContentModel> getLoadedContentByHashAndFilename(Set<Pair<String, String>> hashAndFileNames) {
        final var filenamehashes =hashAndFileNames.stream()
            .map(it -> it.getFirst() + "-" + it.getSecond())
            .collect(Collectors.toSet());
        return loadedContentRepository.findByFilenamehashes(filenamehashes)
                .stream()
                .map(loadedContentMapper::entityToModel)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean deleteLoadedContentById(final Long id) {
        log.info("Удаление загруженного контента по ID: {}", id);
        
        if (!loadedContentRepository.existsById(id)) {
            log.warn("Загруженный контент с ID {} не найден", id);
            return false;
        }
        
        loadedContentRepository.deleteById(id);
        log.info("Загруженный контент с ID {} успешно удален", id);
        return true;
    }
    
    @Override
    public Optional<LoadedContentModel> getLoadedContentById(final Long id) {
        log.debug("Получение загруженного контента по ID: {}", id);
        
        return loadedContentRepository.findById(id)
                .map(loadedContentMapper::entityToModel);
    }

}
