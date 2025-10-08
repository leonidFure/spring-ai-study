package com.example.springaiapp.domain.loadedcontent.service;

/*
 * Сервис для загрузки контента из файлов
 * Загружает контент из файлов и сохраняет его в векторное хранилище
 * Контент может быть загружен из файлов
 */
public interface FileContentLoadService  {

    /**
     * Загружает контент из файлов и сохраняет его в векторное хранилище
     * 
     */
    void loadFilesContent();

}
