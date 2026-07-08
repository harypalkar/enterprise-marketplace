# Coding Standards

## General

- Target **Java 21** with modern language features (records, pattern matching, virtual threads where applicable)
- Follow **SOLID** principles and **Clean Code** practices
- No placeholder or stub implementations in production paths
- All public APIs must have OpenAPI documentation

## Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Package | lowercase, singular nouns | `com.enterprise.marketplace.product.domain` |
| Class | PascalCase | `ProductService` |
| Interface (Port) | PascalCase, no `I` prefix | `ProductRepository` |
| Method | camelCase, verb-first | `findProductById` |
| Constant | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| REST endpoint | kebab-case, plural nouns | `/api/v1/products` |
| Database table | snake_case, singular | `product` |
| Database column | snake_case | `created_at` |

## Package Structure (Per Service)

```
com.enterprise.marketplace.{service}/
├── domain/
│   ├── model/
│   ├── port/
│   └── service/
├── application/
│   ├── dto/
│   ├── mapper/
│   └── service/
├── infrastructure/
│   ├── persistence/
│   ├── messaging/
│   └── config/
└── bootstrap/
    ├── controller/
    └── config/
```

## Layer Rules

1. **Domain** must not depend on Spring, JPA, or infrastructure frameworks
2. **Application** orchestrates use cases; depends on domain ports only
3. **Infrastructure** implements ports; contains all framework-specific code
4. **Bootstrap** wires everything together; controllers are thin adapters

## API Design

- Version all APIs: `/api/v1/`
- Use `ApiResponse<T>` wrapper for success responses
- Use `ErrorResponse` for error responses (via `GlobalExceptionHandler`)
- Return appropriate HTTP status codes
- Support pagination: `page`, `size`, `sort` query parameters
- Include `X-Correlation-Id` and `X-Request-Id` in all responses
- Mutating POST/PUT/PATCH endpoints must support `Idempotency-Key` header

## Exception Handling

- Throw `MarketplaceException` with appropriate `ErrorCode`
- Use `ResourceNotFoundException` for 404 scenarios
- Never expose stack traces or internal details in API responses
- Log exceptions with correlation context

## Validation

- Use Jakarta Bean Validation (`@Valid`, `@NotNull`, etc.) on request DTOs
- Use `ValidationUtility` for cross-cutting validations (GSTIN, PAN, mobile)
- Validate at the application layer boundary

## Testing

- Unit tests for domain logic and application services
- Integration tests with Testcontainers for persistence and messaging
- Minimum 80% coverage on domain and application layers
- Test naming: `should{ExpectedBehavior}When{Condition}`

## Code Formatting

- **Spotless** with Google Java Format (AOSP style)
- **Checkstyle** enforced in CI (warnings initially)
- Run `mvn spotless:apply` before committing

## Git Commit Messages

```
type(scope): concise description

feat(product): add product creation API
fix(inventory): resolve stock deduction race condition
docs(readme): update quick start guide
refactor(common): extract idempotency store interface
test(pricing): add bulk pricing calculation tests
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `ci`, `build`

## Dependencies

- Prefer Spring Boot starters over manual dependency assembly
- No SNAPSHOT dependencies in production releases
- Document any non-standard dependency choices in PR description

## Security

- Never log sensitive data (passwords, tokens, PII)
- Use environment variables for secrets
- Validate and sanitize all external input
- Apply principle of least privilege for service accounts
