package com.enterprise.marketplace.adminservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import com.enterprise.marketplace.adminservice.entity.PlatformSettingEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminMapperTest {

    private final AdminMapper adminMapper = new AdminMapper(new ObjectMapper());

    @Test
    void shouldMapSettingEntityToResponse() {
        PlatformSettingEntity entity = new PlatformSettingEntity();
        entity.setId(UUID.randomUUID());
        entity.setSettingKey("marketplace.name");
        entity.setSettingValue("Enterprise Marketplace");
        entity.setCategory("GENERAL");
        entity.setDescription("Platform name");
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        SettingResponse response = adminMapper.toSettingResponse(entity);

        assertThat(response.getSettingKey()).isEqualTo("marketplace.name");
        assertThat(response.getSettingValue()).isEqualTo("Enterprise Marketplace");
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void shouldSerializeAndDeserializeJsonMap() {
        Map<String, Object> value = Map.of("timeout", 30, "retries", 3);
        String json = adminMapper.serializeJsonMap(value);
        Map<String, Object> deserialized = adminMapper.deserializeJsonMap(json);

        assertThat(deserialized).containsEntry("timeout", 30).containsEntry("retries", 3);
    }
}
