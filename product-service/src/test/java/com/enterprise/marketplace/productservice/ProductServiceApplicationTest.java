package com.enterprise.marketplace.productservice;

import com.enterprise.marketplace.productservice.repository.ProductRepository;
import com.enterprise.marketplace.productservice.service.impl.ProductServiceImpl;
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
    "spring.profiles.active=test",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false"
})
class ProductServiceApplicationTest {

    @MockBean
    private ProductServiceImpl productServiceImpl;

    @MockBean
    private ProductRepository productRepository;

    @Test
    void contextLoads() {
    }
}
