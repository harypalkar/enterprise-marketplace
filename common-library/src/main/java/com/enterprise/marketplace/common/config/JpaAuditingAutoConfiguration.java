package com.enterprise.marketplace.common.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing when Spring Data JPA is fully configured.
 */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(name = "jakarta.persistence.Entity")
@ConditionalOnBean(EntityManagerFactory.class)
@EnableJpaAuditing
public class JpaAuditingAutoConfiguration {
}
