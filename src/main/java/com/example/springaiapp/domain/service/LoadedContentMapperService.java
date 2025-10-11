package com.example.springaiapp.domain.service;

import com.example.springaiapp.domain.model.LoadedContentModel;

public interface LoadedContentMapperService {

    LoadedContentModel createLoadedContentModel(String filename, String hash, int chunkCount);
}
