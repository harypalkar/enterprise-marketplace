# Folder Structure

```
enterprise-marketplace/
├── pom.xml                          # Parent POM with dependency management
├── README.md
├── LICENSE
├── .gitignore
├── .editorconfig
├── checkstyle.xml
├── sonar-project.properties
│
├── docs/                            # Project documentation
│   ├── architecture.md
│   ├── coding-standards.md
│   ├── folder-structure.md
│   ├── logging-standard.md
│   └── branching-strategy.md
│
├── config/                          # Shared configuration
│   ├── checkstyle/
│   ├── spotless/
│   └── templates/                   # application-*.yml templates
│
├── common-library/                    # Shared cross-cutting library
│   └── src/main/java/com/enterprise/marketplace/common/
│       ├── api/                     # ApiResponse, ErrorResponse
│       ├── config/                  # Auto-configuration
│       ├── constant/                # HTTP headers, MDC keys
│       ├── context/                 # RequestContext
│       ├── exception/               # GlobalExceptionHandler, ErrorCode
│       ├── filter/                  # CorrelationIdFilter
│       ├── idempotency/             # Idempotency support
│       ├── model/                   # BaseEntity, AuditModel
│       └── util/                    # LoggingUtility, ValidationUtility
│
├── gateway-service/                 # Spring Cloud Gateway (8080)
├── identity-service/                # Identity service (8081)
├── product-service/                 # Product service (8082)
├── seller-service/                  # Seller service (8083)
├── buyer-service/                   # Buyer service (8084)
├── category-service/                # Category service (8085)
├── inventory-service/               # Inventory service (8086)
├── pricing-service/                 # Pricing service (8087)
├── workflow-service/                # Workflow service (8088)
├── notification-service/            # Notification service (8089)
├── search-service/                  # Search service (8090)
├── ai-service/                      # AI service (8091)
├── audit-service/                   # Audit service (8092)
├── subscription-service/            # Subscription service (8093)
├── report-service/                  # Report service (8094)
├── admin-service/                   # Admin service (8095)
│
├── docker/                          # Docker configurations (future)
├── helm/                            # Helm charts (future)
├── kubernetes/                      # K8s manifests (future)
├── terraform/                       # Infrastructure as code (future)
├── scripts/                         # Build and utility scripts
└── .github/                         # GitHub Actions workflows
```

## Microservice Internal Structure

Each service module follows this layout (to be populated in domain milestones):

```
{service}/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/enterprise/marketplace/{service}/
    │   │   ├── {Service}Application.java
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   ├── port/
    │   │   │   └── service/
    │   │   ├── application/
    │   │   │   ├── dto/
    │   │   │   ├── mapper/
    │   │   │   └── service/
    │   │   ├── infrastructure/
    │   │   │   ├── persistence/
    │   │   │   ├── messaging/
    │   │   │   └── config/
    │   │   └── bootstrap/
    │   │       ├── controller/
    │   │       └── config/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       ├── application-dev.yml
    │       ├── application-qa.yml
    │       └── application-prod.yml
    └── test/
        └── java/
```

## Infrastructure Directories (Bootstrap Placeholders)

| Directory | Purpose |
|-----------|---------|
| `docker/` | Dockerfiles and compose files (Milestone 2) |
| `helm/` | Helm chart definitions for Kubernetes |
| `kubernetes/` | Raw K8s manifests and Kustomize overlays |
| `terraform/` | Cloud infrastructure provisioning |
| `scripts/` | Build, deploy, and utility scripts |
| `.github/workflows/` | CI/CD pipeline definitions |
