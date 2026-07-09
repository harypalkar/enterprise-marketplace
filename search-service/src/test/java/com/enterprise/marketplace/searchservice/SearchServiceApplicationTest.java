package com.enterprise.marketplace.searchservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration",
    "spring.profiles.active=test",
    "marketplace.elasticsearch.enabled=false"
})
class SearchServiceApplicationTest {

    @Test
    void contextLoads() {
    }
}
