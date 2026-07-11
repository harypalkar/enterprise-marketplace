package com.enterprise.marketplace.aiservice.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatResponse {

    String sessionKey;
    String reply;
    String model;
    Long latencyMs;
    List<ChatMessageDto> history;
}
