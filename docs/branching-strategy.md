# Branching Strategy

## Model: GitFlow (Adapted for Microservices Monorepo)

```
main ─────────────────────────────────────────────► production releases
  │
  └── develop ────────────────────────────────────► integration branch
        │
        ├── feature/EMP-123-product-api ──────────► new features
        ├── bugfix/EMP-456-stock-race ────────────► non-critical fixes
        └── release/1.0.0 ────────────────────────► release preparation
```

## Branches

| Branch | Purpose | Merge Target | Protected |
|--------|---------|--------------|-----------|
| `main` | Production-ready code | — | Yes |
| `develop` | Integration branch | `main` (via release) | Yes |
| `feature/*` | New features | `develop` | No |
| `bugfix/*` | Bug fixes | `develop` | No |
| `hotfix/*` | Production emergencies | `main` + `develop` | No |
| `release/*` | Release stabilization | `main` + `develop` | No |

## Naming Convention

```
feature/EMP-{ticket}-{short-description}
bugfix/EMP-{ticket}-{short-description}
hotfix/EMP-{ticket}-{short-description}
release/{version}
```

Examples:
- `feature/EMP-101-seller-registration-api`
- `bugfix/EMP-205-inventory-negative-stock`
- `hotfix/EMP-301-auth-token-expiry`
- `release/1.2.0`

## Workflow

### Feature Development

1. Branch from `develop`: `git checkout -b feature/EMP-123-product-api develop`
2. Implement with atomic commits
3. Open Merge Request (GitLab) / Pull Request (GitHub) to `develop`
4. Require: CI pass, code review approval, no merge conflicts
5. Squash merge preferred for clean history

### Release

1. Branch from `develop`: `git checkout -b release/1.0.0 develop`
2. Bump version in parent `pom.xml`
3. Final testing and bug fixes on release branch only
4. Merge to `main` with tag `v1.0.0`
5. Merge back to `develop`

### Hotfix

1. Branch from `main`: `git checkout -b hotfix/EMP-301-auth-fix main`
2. Fix and test
3. Merge to `main` with patch tag
4. Cherry-pick or merge to `develop`

## Commit Rules

- Conventional commits format (see coding-standards.md)
- One logical change per commit
- Reference ticket: `feat(product): add SKU validation EMP-123`

## Merge Request Requirements

- [ ] CI pipeline green (build, test, checkstyle, spotless)
- [ ] SonarQube quality gate passed
- [ ] At least one approving review
- [ ] No `SNAPSHOT` dependencies introduced without justification
- [ ] Documentation updated if API or architecture changed

## Versioning

Semantic Versioning (SemVer): `MAJOR.MINOR.PATCH`

- **MAJOR** — breaking API changes
- **MINOR** — backward-compatible features
- **PATCH** — backward-compatible bug fixes

Parent POM version drives all module versions in this monorepo.

## Environment Mapping

| Branch/Tag | Environment | Deployment |
|------------|-------------|------------|
| `develop` | Dev | Auto-deploy on merge |
| `release/*` | QA | Auto-deploy on push |
| `main` / tags | Production | Manual approval gate |

## Monorepo Considerations

- A single MR may touch multiple modules — ensure all affected modules build
- Use `mvn -pl {module} -am` to build module with dependencies
- Cross-service changes should be coordinated within a single feature branch when possible
