package com.enterprise.marketplace.aiservice.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessageDto {

    String role;
    String content;
}
