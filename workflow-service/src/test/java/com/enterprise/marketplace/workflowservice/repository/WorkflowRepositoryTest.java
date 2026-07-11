package com.enterprise.marketplace.workflowservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowTransitionEntity;
import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.flyway.enabled=true")
class WorkflowRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowTransitionRepository transitionRepository;

    @Test
    void shouldPersistAndFindWorkflowByRequestId() {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setRequestId("repo-req-1");
        entity.setAggregateType(AggregateType.PRODUCT);
        entity.setAggregateId(UUID.randomUUID());
        entity.setOperationType(WorkflowOperationType.CREATE);
        entity.setStatus(WorkflowStatus.INITIAL);
        entity.setActive(true);

        workflowRepository.save(entity);

        assertThat(workflowRepository.findByRequestIdAndActiveTrue("repo-req-1")).isPresent();
    }

    @Test
    void shouldLoadSeedTransitionRules() {
        assertThat(transitionRepository.existsByFromStatusAndToStatusAndActiveTrue(
                        WorkflowStatus.INITIAL, WorkflowStatus.RECEIVED))
                .isTrue();
        assertThat(transitionRepository.findByActiveTrue()).isNotEmpty();
    }
}
