package com.example.springaiapp.domain.service.impl;

import org.springframework.stereotype.Service;

import com.example.springaiapp.domain.model.LoadedContentModel;
import com.example.springaiapp.domain.service.LoadedContentMapperService;

@Service
public class LoadedContentMapperServiceImpl implements LoadedContentMapperService {

    @Override
    public LoadedContentModel createLoadedContentModel(String filename, String hash, int chunkCount) {
        return LoadedContentModel.builder()
                .filename(filename)
                .type("txt")
                .hash(hash)
                .chunkCount(chunkCount)
                .build();
    }

}
