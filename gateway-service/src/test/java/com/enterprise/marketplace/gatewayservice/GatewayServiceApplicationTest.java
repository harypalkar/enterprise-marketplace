package com.enterprise.marketplace.gatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
    "marketplace.security.enabled=false"
})
class GatewayServiceApplicationTest {

    @Test
    void contextLoads() {
    }
}
