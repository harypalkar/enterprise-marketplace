$root = "d:\Harish nilam medical documents\pandit doctor receipt (1)\harish\enterprise-marketplace"

$services = @(
    @{ Name = "gateway-service"; Port = 8080; Gateway = $true; Jpa = $false },
    @{ Name = "identity-service"; Port = 8081; Gateway = $false; Jpa = $true },
    @{ Name = "product-service"; Port = 8082; Gateway = $false; Jpa = $true },
    @{ Name = "seller-service"; Port = 8083; Gateway = $false; Jpa = $true },
    @{ Name = "buyer-service"; Port = 8084; Gateway = $false; Jpa = $true },
    @{ Name = "category-service"; Port = 8085; Gateway = $false; Jpa = $true },
    @{ Name = "inventory-service"; Port = 8086; Gateway = $false; Jpa = $true },
    @{ Name = "pricing-service"; Port = 8087; Gateway = $false; Jpa = $true },
    @{ Name = "workflow-service"; Port = 8088; Gateway = $false; Jpa = $true },
    @{ Name = "notification-service"; Port = 8089; Gateway = $false; Jpa = $true },
    @{ Name = "search-service"; Port = 8090; Gateway = $false; Jpa = $false },
    @{ Name = "ai-service"; Port = 8091; Gateway = $false; Jpa = $false },
    @{ Name = "audit-service"; Port = 8092; Gateway = $false; Jpa = $true },
    @{ Name = "subscription-service"; Port = 8093; Gateway = $false; Jpa = $true },
    @{ Name = "report-service"; Port = 8094; Gateway = $false; Jpa = $true },
    @{ Name = "admin-service"; Port = 8095; Gateway = $false; Jpa = $true }
)

function Get-ClassName($serviceName) {
    $parts = $serviceName -replace '-service$','' -split '-'
    $camel = ($parts | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join ''
    return "${camel}ServiceApplication"
}

function Get-PackageSuffix($serviceName) {
    return ($serviceName -replace '-','')
}

foreach ($svc in $services) {
    $name = $svc.Name
    $port = $svc.Port
    $className = Get-ClassName $name
    $pkg = "com.enterprise.marketplace.$(Get-PackageSuffix $name)"
    $pkgPath = $pkg -replace '\.','/'
    $svcRoot = Join-Path $root $name

    # POM
    $webDep = if ($svc.Gateway) { "spring-cloud-starter-gateway" } else { "spring-boot-starter-web" }
    $jpaBlock = if ($svc.Jpa) {
@"
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
"@
    } else { "" }

    $openapiDep = if ($svc.Gateway) {
        "springdoc-openapi-starter-webflux-ui"
    } else {
        "springdoc-openapi-starter-webmvc-ui"
    }

    $pom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.enterprise.marketplace</groupId>
        <artifactId>enterprise-marketplace</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>$name</artifactId>
    <name>$(($name -replace '-',' ') -replace '\b(\w)', { $_.Value.ToUpper() })</name>
    <description>$name microservice for Enterprise Marketplace Platform</description>

    <dependencies>
        <dependency>
            <groupId>com.enterprise.marketplace</groupId>
            <artifactId>common-library</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>$webDep</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-otel</artifactId>
        </dependency>
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-otlp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>$openapiDep</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
$jpaBlock
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
"@

    Set-Content -Path (Join-Path $svcRoot "pom.xml") -Value $pom -Encoding UTF8

    # Application class
    $scanPackages = if ($svc.Gateway) {
        "@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })"
    } elseif (-not $svc.Jpa) {
        "@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })"
    } else {
        "@SpringBootApplication"
    }

    $imports = if (-not $svc.Jpa) {
        @"
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
"@
    } else { "" }

    $appJava = @"
package $pkg;

import com.enterprise.marketplace.common.util.LoggingUtility;
$imports
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class $className {

    public static void main(String[] args) {
        SpringApplication.run($className.class, args);
        LoggingUtility.setServiceName("$name");
    }
}
"@

    if (-not $svc.Jpa) {
        $appJava = $appJava -replace '@SpringBootApplication', '@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })'
    }

    $javaDir = Join-Path $svcRoot "src\main\java\$($pkgPath -replace '/','\')"
    New-Item -ItemType Directory -Force -Path $javaDir | Out-Null
    Set-Content -Path (Join-Path $javaDir "$className.java") -Value $appJava -Encoding UTF8

    # Bootstrap health/info controller - minimal, not business API
    if (-not $svc.Gateway) {
        $controllerJava = @"
package $pkg.bootstrap;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.context.RequestContext;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bootstrap")
public class BootstrapController {

    @Value("`${spring.application.name}")
    private String serviceName;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> payload = Map.of(
                "service", serviceName,
                "status", "UP",
                "correlationId", RequestContext.getCorrelationId(),
                "requestId", RequestContext.getRequestId());
        ApiResponse<Map<String, String>> response = ApiResponse.success(payload, "Service bootstrap health check");
        response.setCorrelationId(RequestContext.getCorrelationId());
        response.setRequestId(RequestContext.getRequestId());
        return ResponseEntity.ok(response);
    }
}
"@
        $bootstrapDir = Join-Path $javaDir "bootstrap"
        New-Item -ItemType Directory -Force -Path $bootstrapDir | Out-Null
        Set-Content -Path (Join-Path $bootstrapDir "BootstrapController.java") -Value $controllerJava -Encoding UTF8
    }

    # YAML files
    $resDir = Join-Path $svcRoot "src\main\resources"
    $jpaYaml = if ($svc.Jpa) {
@"
  datasource:
    url: `${NEON_DB_URL:jdbc:postgresql://localhost:5432/${name.replace('-','_')}}
    username: `${NEON_DB_USER:marketplace}
    password: `${NEON_DB_PASSWORD:marketplace}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      pool-name: ${name}-pool
"@
    } else {
        ""
    }

    $gatewayYaml = if ($svc.Gateway) {
@"
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=X-Correlation-Id, `$`{random.uuid}
      routes: []
"@
    } else { "" }

    $yml = @"
spring:
  application:
    name: $name
  profiles:
    active: `${SPRING_PROFILES_ACTIVE:local}
  jackson:
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null
$jpaYaml
$gatewayYaml
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
  flyway:
    enabled: false

server:
  port: $port
  shutdown: graceful
  error:
    include-message: never
    include-binding-errors: never
    include-stacktrace: never

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when_authorized
  metrics:
    tags:
      application: `${spring.application.name}
  tracing:
    sampling:
      probability: `${TRACING_SAMPLING_PROBABILITY:1.0}
  otlp:
    tracing:
      endpoint: `${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/traces}

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: false

logging:
  pattern:
    level: "%5p [`${spring.application.name:},%X{correlationId:-},%X{requestId:-}]"
  level:
    root: INFO
    com.enterprise.marketplace: INFO

marketplace:
  service:
    name: $name
"@

    if (-not $svc.Jpa) {
        $yml = $yml -replace "(?ms)  jpa:.*?  flyway:", "  flyway:"
    }

    Set-Content -Path (Join-Path $resDir "application.yml") -Value $yml -Encoding UTF8

    $localYml = @"
spring:
  jpa:
    show-sql: true

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    com.enterprise.marketplace: DEBUG

management:
  endpoint:
    health:
      show-details: always
"@
    Set-Content -Path (Join-Path $resDir "application-local.yml") -Value $localYml -Encoding UTF8
    Set-Content -Path (Join-Path $resDir "application-dev.yml") -Value "logging:`n  level:`n    com.enterprise.marketplace: DEBUG" -Encoding UTF8
    Set-Content -Path (Join-Path $resDir "application-qa.yml") -Value "logging:`n  level:`n    com.enterprise.marketplace: INFO" -Encoding UTF8
    Set-Content -Path (Join-Path $resDir "application-prod.yml") -Value @"
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false

logging:
  level:
    root: WARN
    com.enterprise.marketplace: INFO
"@ -Encoding UTF8

    # Test
    $testPkg = $pkg
    $testDir = Join-Path $svcRoot "src\test\java\$($pkgPath -replace '/','\')"
    New-Item -ItemType Directory -Force -Path $testDir | Out-Null

    $testAnnotations = if ($svc.Gateway -or -not $svc.Jpa) {
        "@SpringBootTest(properties = {`"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration`"})"
    } else {
        "@SpringBootTest"
    }

    $testJava = @"
package $testPkg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

$testAnnotations
class ${className}Test {

    @Test
    void contextLoads() {
    }
}
"@
    Set-Content -Path (Join-Path $testDir "${className}Test.java") -Value $testJava -Encoding UTF8

    Write-Output "Generated $name"
}

Write-Output "All services generated"
