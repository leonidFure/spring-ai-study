package com.example.springaiapp.domain.loadedcontent.service;

import com.example.springaiapp.domain.loadedcontent.model.LoadedContentModel;

public interface LoadedContentMapperService {

    LoadedContentModel createLoadedContentModel(String filename, String hash, int chunkCount);
}
