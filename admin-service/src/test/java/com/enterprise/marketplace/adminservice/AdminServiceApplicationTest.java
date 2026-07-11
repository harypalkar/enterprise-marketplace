package com.enterprise.marketplace.adminservice;

import com.enterprise.marketplace.adminservice.repository.AdminConfigRepository;
import com.enterprise.marketplace.adminservice.repository.FeatureFlagRepository;
import com.enterprise.marketplace.adminservice.repository.PlatformSettingRepository;
import com.enterprise.marketplace.adminservice.service.impl.AdminServiceImpl;
import com.enterprise.marketplace.adminservice.validation.AdminRequestValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false"
})
class AdminServiceApplicationTest {

    @MockBean
    private AdminServiceImpl adminServiceImpl;

    @MockBean
    private PlatformSettingRepository platformSettingRepository;

    @MockBean
    private FeatureFlagRepository featureFlagRepository;

    @MockBean
    private AdminConfigRepository adminConfigRepository;

    @MockBean
    private AdminRequestValidator adminRequestValidator;

    @Test
    void contextLoads() {}
}
