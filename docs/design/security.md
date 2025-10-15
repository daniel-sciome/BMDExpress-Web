# Security Considerations

## Authentication

**Strategy:** JWT (JSON Web Tokens)

**Flow:**
1. User logs in with username/password
2. Server validates credentials
3. Server generates JWT with user claims
4. Client stores JWT (HTTP-only cookie or localStorage)
5. Client includes JWT in Authorization header for subsequent requests

**JWT Claims:**
```json
{
  "sub": "user123",
  "email": "researcher@university.edu",
  "roles": ["USER"],
  "exp": 1730000000
}
```

## Authorization

**Role-Based Access Control (RBAC):**

| Role | Permissions |
|------|-------------|
| **USER** | Create/manage own projects, run analyses |
| **ADMIN** | All USER permissions + manage all projects, view all jobs |
| **GUEST** | Read-only access to public projects |

**Implementation:**
```java
@PreAuthorize("hasRole('USER')")
public BMDProject createProject(ProjectRequest request) { ... }

@PreAutize("hasRole('ADMIN') or @projectSecurity.isOwner(#projectId, authentication)")
public void deleteProject(Long projectId) { ... }
```

## Data Isolation

**Multi-Tenancy Strategy:**
- Projects owned by users (user_id foreign key)
- Row-level security: queries automatically filtered by user
- Spring Security context used in service layer
- Repository methods enforce ownership checks

**Example:**
```java
public interface BMDProjectRepository extends JpaRepository<BMDProject, Long> {

    @Query("SELECT p FROM BMDProject p WHERE p.owner.id = :userId")
    List<BMDProject> findByUserId(@Param("userId") Long userId);
}
```

## Input Validation

**Validation Strategy:**
- Bean Validation (JSR-380) annotations on DTOs
- Custom validators for domain logic
- Sanitize file uploads (content type, size limits)
- Parameterized SQL queries (prevent SQL injection)

**Example:**
```java
public class BMDAnalysisRequest {

    @NotNull
    @Positive
    private Long projectId;

    @NotNull
    @Positive
    private Long experimentId;

    @NotEmpty
    private Set<StatModel> models;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double bmr = 1.349;
}
```

## Rate Limiting

**Strategy:** Redis-backed rate limiter

**Limits:**
- 100 API requests per minute per user
- 10 analysis jobs per hour per user
- 5 concurrent jobs per user

## Security Checklist

- [ ] HTTPS enforced (TLS 1.3)
- [ ] CSRF protection enabled
- [ ] XSS protection (Content Security Policy)
- [ ] SQL injection prevention (parameterized queries)
- [ ] Secure password hashing (bcrypt)
- [ ] JWT signed with strong secret
- [ ] File upload validation (type, size, virus scan)
- [ ] Rate limiting implemented
- [ ] Audit logging (sensitive operations)
- [ ] Regular dependency updates (vulnerability scanning)
- [ ] Secrets managed via environment variables/vault
