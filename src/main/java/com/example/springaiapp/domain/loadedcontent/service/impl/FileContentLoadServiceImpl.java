package com.example.springaiapp.domain.loadedcontent.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.example.springaiapp.domain.loadedcontent.model.LoadedContentModel;
import com.example.springaiapp.domain.loadedcontent.service.FileContentLoadService;
import com.example.springaiapp.domain.loadedcontent.service.LoadedContentMapperService;
import com.example.springaiapp.domain.loadedcontent.service.LoadedContentService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileContentLoadServiceImpl implements FileContentLoadService {
    private final LoadedContentService loadedContentService;
    private final LoadedContentMapperService loadedContentMapperService;
    private final ResourcePatternResolver resourcePatternResolver;
    private final VectorStore vectorStore;
    private final TextSplitter textSplitter;

    @PostConstruct
    public void init() {
        loadFilesContent();
    }

    /**
     * Загружает контент из файлов
     * разбивает на чанки и сохраняет в векторное хранилище
     * сохраняет в базу данных
     */
    @Override
    public void loadFilesContent() {
        // todo подумать как оптимизировать
        try {
            final var filesMap = Arrays
                    .stream(resourcePatternResolver.getResources("classpath*:/knowlegebase/**/*.txt"))
                    .collect(Collectors.toMap(
                            resource -> Pair.of(Objects.requireNonNull(resource.getFilename()),
                                    calculateFileHash(resource)),
                            Function.identity(), (a, b) -> a));
            if (filesMap.isEmpty()) {
                log.info("No files to load");
                return;
            }
            final var loadedContentMap = loadedContentService.getLoadedContentByHashAndFilename(filesMap.keySet())
                    .stream()
                    .collect(Collectors.toMap(
                            it -> Pair.of(it.getFilename(), it.getHash()),
                            Function.identity(), (a, b) -> a));

            final var loadedContentList = filesMap.entrySet().stream()
                    .filter(it -> !loadedContentMap.containsKey(it.getKey()))
                    .map(it -> processResource(it))
                    .collect(Collectors.toList());

            loadedContentService.addLoadedContentList(loadedContentList);
        } catch (IOException e) {
            log.error("Failed to load files content", e);
        }
    }

    private String calculateFileHash(final Resource resource) {
        try (final var is = resource.getInputStream()) {
            return DigestUtils.md5DigestAsHex(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }

    private LoadedContentModel processResource(final Map.Entry<Pair<String, String>, Resource> entry) {
        log.info("Processing resource: {}", entry.getKey().getFirst());
        final var document = new TextReader(entry.getValue()).get();
        final var chunks = textSplitter.apply(document);
        // в стартере уже есть репозиторий и ентити, нам надо создать таблицу
        // vector_store, если зохотим другое название, то это можно настроить
        // 
        vectorStore.accept(chunks);
        return loadedContentMapperService.createLoadedContentModel(
                entry.getKey().getFirst(),
                entry.getKey().getSecond(),
                chunks.size());
    }
}
