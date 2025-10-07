package com.example.springaiapp.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiMessageResponse {
    private final ResponseMessageType messageType;
    private final String message;

    public static AiMessageResponse start(String message) {
        return AiMessageResponse.builder()
                .messageType(ResponseMessageType.START)
                .message(message)
                .build();
    }

    public static AiMessageResponse userMessage(String message) {
        return AiMessageResponse.builder()
                .messageType(ResponseMessageType.USER_MESSAGE)
                .message(message)
                .build();
    }
    
    public static AiMessageResponse aiMessage(String message) {
        return AiMessageResponse.builder()
                .messageType(ResponseMessageType.AI_MESSAGE)
                .message(message)
                .build();
    }
    
    
    public static AiMessageResponse complete(String message) {
        return AiMessageResponse.builder()
                .messageType(ResponseMessageType.COMPLETE)
                .message(message)
                .build();
    }
}
