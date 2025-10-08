package com.example.springaiapp.infrastracture.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.springaiapp.infrastracture.entity.LoadedContentEntity;

/**
 * Репозиторий для работы с сущностью LoadedContent
 * Предоставляет методы для CRUD операций с загруженным контентом
 */
@Repository
public interface LoadedContentRepository extends JpaRepository<LoadedContentEntity, Long> {

    /**
     * Поиск загруженного контента по хешу и имени файла
     * 
     * @param hash     хеш файла
     * @param filename имя файла
     * @return Optional с найденным контентом
     */
    Optional<LoadedContentEntity> findByHashAndFilename(String hash, String filename);

    /**
     * Поиск загруженного контента по типу
     * 
     * @param type тип контента
     * @return список контента указанного типа
     */
    List<LoadedContentEntity> findByType(String type);

    /**
     * Проверка существования контента по хешу и имени файла
     * 
     * @param hash     хеш файла
     * @param filename имя файла
     * @return true если контент существует
     */
    boolean existsByHashAndFilename(String hash, String filename);

    /**
     * Массовое получение загруженного контента по списку хешей и имен файлов
     *
     * @param hashes множество хешей
     * @param filenames множество имен файлов
     * @return список найденных сущностей LoadedContentEntity
     */
    @Query("SELECT lc FROM LoadedContentEntity lc WHERE lc.filename || '-' || lc.hash IN :filenamehashes")
    List<LoadedContentEntity> findByFilenamehashes(@Param("filenamehashes") Set<String> filenamehashes);
}
