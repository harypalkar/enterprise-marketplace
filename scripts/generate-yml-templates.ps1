# Shared YAML templates for microservices
$base = "d:\Harish nilam medical documents\pandit doctor receipt (1)\harish\enterprise-marketplace\config\templates"

$applicationYml = @'
spring:
  application:
    name: {{SERVICE_NAME}}
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
  jackson:
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
        format_sql: false
  flyway:
    enabled: false

server:
  port: {{PORT}}
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
      application: ${spring.application.name}
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING_PROBABILITY:1.0}
  otlp:
    tracing:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/traces}

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: false

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{correlationId:-},%X{requestId:-}]"
  level:
    root: INFO
    com.enterprise.marketplace: INFO

marketplace:
  service:
    name: {{SERVICE_NAME}}
'@

$applicationLocalYml = @'
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    com.enterprise.marketplace: DEBUG
    org.springframework.web: DEBUG

management:
  endpoint:
    health:
      show-details: always
'@

$applicationDevYml = @'
logging:
  level:
    com.enterprise.marketplace: DEBUG

management:
  tracing:
    sampling:
      probability: 1.0
'@

$applicationQaYml = @'
logging:
  level:
    com.enterprise.marketplace: INFO

management:
  tracing:
    sampling:
      probability: 0.5
'@

$applicationProdYml = @'
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false

logging:
  level:
    root: WARN
    com.enterprise.marketplace: INFO

management:
  endpoint:
    health:
      show-details: never
  tracing:
    sampling:
      probability: 0.1
'@

Set-Content -Path "$base\application.yml.template" -Value $applicationYml -Encoding UTF8
Set-Content -Path "$base\application-local.yml.template" -Value $applicationLocalYml -Encoding UTF8
Set-Content -Path "$base\application-dev.yml.template" -Value $applicationDevYml -Encoding UTF8
Set-Content -Path "$base\application-qa.yml.template" -Value $applicationQaYml -Encoding UTF8
Set-Content -Path "$base\application-prod.yml.template" -Value $applicationProdYml -Encoding UTF8

Write-Output "Templates created"
