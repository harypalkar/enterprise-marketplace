# GitHub Setup

Enterprise Marketplace Platform — source control configuration and workflow guide.

## Repository URL

| Item | Value |
|------|-------|
| **GitHub Account** | [harypalkar](https://github.com/harypalkar) |
| **Repository** | [enterprise-marketplace](https://github.com/harypalkar/enterprise-marketplace) |
| **Clone (HTTPS)** | `https://github.com/harypalkar/enterprise-marketplace.git` |
| **Clone (SSH)** | `git@github.com:harypalkar/enterprise-marketplace.git` |
| **Default Branch** | `main` |

### Clone the Repository

```bash
git clone https://github.com/harypalkar/enterprise-marketplace.git
cd enterprise-marketplace
```

### Verify Remote

```bash
git remote -v
```

Expected output:

```
origin  https://github.com/harypalkar/enterprise-marketplace.git (fetch)
origin  https://github.com/harypalkar/enterprise-marketplace.git (push)
```

---

## Branch Strategy

This project follows **GitFlow (adapted for monorepo)**. Full details are in [branching-strategy.md](./branching-strategy.md).

```
main ─────────────────────────────────────────────► production releases
  │
  └── develop ────────────────────────────────────► integration branch
        │
        ├── feature/EMP-123-product-api
        ├── bugfix/EMP-456-stock-race
        └── release/1.0.0
```

| Branch | Purpose | Merge Target | Protected |
|--------|---------|--------------|-----------|
| `main` | Production-ready code | — | Yes |
| `develop` | Integration branch | `main` (via release) | Yes |
| `feature/*` | New features | `develop` | No |
| `bugfix/*` | Non-critical fixes | `develop` | No |
| `hotfix/*` | Production emergencies | `main` + `develop` | No |
| `release/*` | Release stabilization | `main` + `develop` | No |

### Branch Naming

```
feature/EMP-{ticket}-{short-description}
bugfix/EMP-{ticket}-{short-description}
hotfix/EMP-{ticket}-{short-description}
release/{version}
```

Examples:

- `feature/EMP-101-seller-registration-api`
- `bugfix/EMP-205-inventory-negative-stock`
- `release/1.0.0`

---

## Git Workflow

### Daily Development

1. Sync with integration branch:
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. Create a feature branch:
   ```bash
   git checkout -b feature/EMP-123-product-api develop
   ```

3. Commit changes with conventional commit messages (see below).

4. Push and open a Pull Request to `develop`:
   ```bash
   git push -u origin feature/EMP-123-product-api
   ```

5. After review and CI pass, squash-merge into `develop`.

### Pull Request Checklist

- [ ] CI pipeline green (build, test, Spotless, Checkstyle)
- [ ] SonarQube quality gate passed (when configured)
- [ ] At least one approving review
- [ ] Affected modules build: `mvn -pl {module} -am clean verify`
- [ ] No secrets or credentials committed

### Hotfix Workflow

```bash
git checkout -b hotfix/EMP-301-auth-fix main
# fix, test, commit
git push -u origin hotfix/EMP-301-auth-fix
# merge to main with tag, then cherry-pick or merge to develop
```

### Authentication

Use **Git Credential Manager** or **GitHub CLI**:

```bash
gh auth login
```

Do not store tokens or passwords in repository files. Use environment variables and GitHub Secrets for CI/CD.

---

## Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/) aligned with [coding-standards.md](./coding-standards.md).

### Format

```
type(scope): concise description

Optional body explaining why the change was made.

Refs: EMP-123
```

### Types

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no logic change |
| `refactor` | Code change without feature/fix |
| `test` | Adding or updating tests |
| `chore` | Build, tooling, dependencies |
| `ci` | CI/CD pipeline changes |
| `build` | Maven/build configuration |

### Examples

```
feat(product): add product creation API
fix(inventory): resolve stock deduction race condition
docs(readme): update quick start guide
refactor(common): extract idempotency store interface
ci(github): add Maven build workflow
chore(deps): bump Spring Boot to 3.5.1
```

### Rules

- Use imperative mood: "add" not "added"
- Keep subject line under 72 characters
- Reference ticket ID when applicable (`EMP-123`)
- One logical change per commit when possible

---

## Release Strategy

### Versioning

**Semantic Versioning (SemVer):** `MAJOR.MINOR.PATCH`

| Segment | When to Increment |
|---------|-------------------|
| **MAJOR** | Breaking API or contract changes |
| **MINOR** | Backward-compatible features |
| **PATCH** | Backward-compatible bug fixes |

The parent `pom.xml` version (`1.0.0-SNAPSHOT`) drives all module versions in this monorepo.

### Release Process

1. **Create release branch** from `develop`:
   ```bash
   git checkout -b release/1.0.0 develop
   ```

2. **Stabilize** — bump version in `pom.xml`, final QA, bug fixes on release branch only.

3. **Merge to `main`** and tag:
   ```bash
   git checkout main
   git merge release/1.0.0
   git tag -a v1.0.0 -m "Release 1.0.0 - Enterprise Marketplace Bootstrap"
   git push origin main --tags
   ```

4. **Merge back to `develop`**:
   ```bash
   git checkout develop
   git merge release/1.0.0
   git push origin develop
   ```

5. **Bump to next SNAPSHOT** on `develop` (e.g. `1.1.0-SNAPSHOT`).

### Environment Mapping

| Branch / Tag | Environment | Deployment |
|--------------|-------------|------------|
| `develop` | Dev | Auto-deploy on merge |
| `release/*` | QA | Auto-deploy on push |
| `main` / tags | Production | Manual approval gate |

### GitHub Releases

Create a GitHub Release for each tag:

1. Go to [Releases](https://github.com/harypalkar/enterprise-marketplace/releases)
2. **Draft a new release** from tag `v1.0.0`
3. Include changelog, migration notes, and deployment instructions

### Milestone Tags (Suggested)

| Tag | Milestone |
|-----|-----------|
| `v1.0.0` | Enterprise Project Bootstrap |
| `v1.1.0` | Infrastructure (Docker Compose, Kafka, Redis) |
| `v1.2.0` | Identity & Keycloak Integration |
| `v2.0.0` | Domain Services (Business APIs) |

---

## Related Documentation

- [Branching Strategy](./branching-strategy.md)
- [Coding Standards](./coding-standards.md)
- [Architecture](./architecture.md)
- [Folder Structure](./folder-structure.md)
