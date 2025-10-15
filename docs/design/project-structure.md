# Project Structure

## Directory Layout

```
BMDExpress-Web/
├── pom.xml                                 # Maven build configuration
├── README.md                               # Project overview
├── DESIGN.md                               # This document
├── docker-compose.yml                      # Local development stack
│
├── src/main/java/com/sciome/bmdexpressweb/
│   │
│   ├── Application.java                    # Spring Boot entry point
│   │
│   ├── config/                             # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── AsyncConfig.java
│   │   ├── RedisConfig.java
│   │   ├── VaadinConfig.java
│   │   └── WebSocketConfig.java
│   │
│   ├── domain/                             # JPA entities
│   │   ├── core/
│   │   ├── prefilter/
│   │   ├── stat/
│   │   ├── category/
│   │   ├── chip/
│   │   └── info/
│   │
│   ├── repository/                         # Spring Data JPA
│   │   ├── BMDProjectRepository.java
│   │   ├── DoseResponseExperimentRepository.java
│   │   └── ...
│   │
│   ├── service/                            # Business logic
│   │   ├── analysis/
│   │   ├── project/
│   │   ├── job/
│   │   └── storage/
│   │
│   ├── service/runner/                     # Async job runners
│   │   ├── ANOVARunner.java
│   │   ├── BMDAnalysisRunner.java
│   │   └── ...
│   │
│   ├── dto/                                # Data Transfer Objects
│   │   ├── request/
│   │   ├── response/
│   │   └── config/
│   │
│   ├── controller/                         # REST API
│   │   ├── ProjectController.java
│   │   ├── AnalysisController.java
│   │   └── ...
│   │
│   ├── views/                              # Vaadin UI
│   │   ├── MainLayout.java
│   │   ├── project/
│   │   ├── data/
│   │   ├── analysis/
│   │   ├── visualization/
│   │   └── components/
│   │
│   ├── events/                             # Spring Events
│   │   ├── analysis/
│   │   └── project/
│   │
│   ├── util/                               # Utilities
│   │   ├── bmds/
│   │   ├── statistics/
│   │   └── annotation/
│   │
│   ├── security/                           # Auth
│   │   ├── UserDetailsServiceImpl.java
│   │   └── SecurityUtils.java
│   │
│   └── exception/                          # Exception handling
│       ├── GlobalExceptionHandler.java
│       └── ...
│
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   ├── application-prod.properties
│   ├── db/migration/                       # Flyway SQL migrations
│   ├── static/                             # Static resources
│   └── annotations/                        # Gene annotations
│
├── src/test/java/                          # Tests
│   ├── service/
│   ├── controller/
│   └── integration/
│
├── docker/                                 # Docker configurations
│   ├── Dockerfile
│   ├── bmds/
│   └── toxicr/
│
└── scripts/                                # Deployment scripts
    ├── setup.sh
    └── migrate-legacy-projects.sh
```

## Package Organization

**Package Naming Convention:** `com.sciome.bmdexpressweb.<layer>.<feature>`

**Layer Separation:**
- `domain`: JPA entities (persistence layer)
- `repository`: Data access interfaces
- `service`: Business logic (application layer)
- `controller`: REST API (presentation layer)
- `views`: Vaadin UI (presentation layer)
- `dto`: Data transfer objects (boundary objects)

**Feature Grouping:**
- `analysis.*`: All analysis-related components
- `project.*`: Project management
- `job.*`: Async job processing
