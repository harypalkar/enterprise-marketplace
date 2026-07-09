package com.enterprise.marketplace.pricingservice;

import com.enterprise.marketplace.pricingservice.domain.port.PricingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
    "spring.profiles.active=test",
    "marketplace.security.enabled=false"
})
class PricingServiceApplicationTest {

    @MockBean
    private PricingRepository pricingRepository;

    @Test
    void contextLoads() {
    }
}
