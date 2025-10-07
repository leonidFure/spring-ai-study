package com.example.springaiapp.api.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessageType {
    START("start"),
    USER_MESSAGE("user_message"),
    AI_MESSAGE("ai_message"),
    COMPLETE("complete");

    @JsonValue
    private final String value;
}
