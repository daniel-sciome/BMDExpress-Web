# BMDExpress-Web Design Document

**Version:** 1.0.0
**Date:** 2025-10-15
**Authors:** Dan Svoboda, Claude Code
**Status:** Draft

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Background](#2-background)
3. [Desktop Application Analysis](#3-desktop-application-analysis)
4. [Web Application Architecture](#4-web-application-architecture)
5. [Migration Strategy](#5-migration-strategy)
6. [Technology Stack](#6-technology-stack)
7. [Project Structure](#7-project-structure)
8. [Implementation Phases](#8-implementation-phases)
9. [Key Components](#9-key-components)
10. [Data Model](#10-data-model)
11. [API Design](#11-api-design)
12. [Security Considerations](#12-security-considerations)
13. [Deployment Architecture](#13-deployment-architecture)
14. [Testing Strategy](#14-testing-strategy)
15. [Risk Mitigation](#15-risk-mitigation)

---

## 1. Executive Summary

BMDExpress-Web is a Spring Boot/Vaadin web application that ports the BMDExpress-3 desktop application to a multi-user, cloud-ready platform. The application enables researchers to analyze high-dimensional dose-response data (particularly gene expression data) using EPA BMDS software and ToxicR models to calculate benchmark dose (BMD) values.

### Key Objectives

- **Multi-user access**: Enable collaborative research across institutions
- **Cloud deployment**: Scalable infrastructure for computationally intensive analyses
- **Modern web UI**: Accessible from any browser without desktop installation
- **API-first design**: Support programmatic access and integrations
- **Maintain scientific integrity**: Preserve validated algorithms from desktop version

### Success Criteria

- 100% feature parity with BMDExpress-3 for core analysis workflows
- Support for concurrent users with isolated workspaces
- Sub-5-second response times for interactive operations
- Successful migration of existing .bm2 project files
- RESTful API for headless batch processing

---

## 2. Background

### 2.1 BMDExpress Project History

BMDExpress is a family of applications for benchmark dose (BMD) analysis of genomic data:

- **BMDExpress 1.0** (2007): Original desktop application by Longlong Yang
- **BMDExpress 2.0** (2018): Updated through NTP/Sciome/Health Canada/EPA collaboration
- **BMDExpress 3.0** (2022): Current JavaFX application with ToxicR integration

### 2.2 Scientific Context

**Benchmark Dose Modeling** estimates the dose at which a biological response deviates from control levels by a predetermined benchmark response (BMR). For genomic data, BMDExpress:

1. Identifies dose-responsive genes through statistical prefiltering
2. Fits dose-response curves using multiple mathematical models
3. Calculates BMD, BMDL (lower confidence limit), and BMDU (upper confidence limit)
4. Performs pathway enrichment analysis to identify affected biological processes
5. Optionally applies IVIVE (In Vitro to In Vivo Extrapolation) for translational toxicology

### 2.3 Current Limitations of Desktop Application

- **Single-user**: No collaboration features
- **Local compute**: Limited by desktop hardware
- **Installation complexity**: Requires Java, R, and native libraries
- **No centralized data**: Projects stored locally
- **Manual updates**: Users must download new versions

---

## 3. Desktop Application Analysis

### 3.1 Architecture Overview

**BMDExpress-3** is a JavaFX desktop application built on Maven with Java 21.

**Location:** `/home/svobodadl/BMDExpress-3/`
**Package:** `com.sciome.bmdexpress2`
**Main Class:** `BMDExpress3Main.java`
**Architecture Pattern:** Model-View-Presenter (MVP)
**Codebase Size:** 550 Java files across 98 packages

### 3.2 Architectural Pattern: MVP

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│    Model     │◄────────│  Presenter   │────────►│     View     │
│ (Domain)     │         │ (Business    │         │  (JavaFX)    │
└──────────────┘         │  Logic)      │         └──────────────┘
                         │              │
                         │              │
                         └──────┬───────┘
                                │
                                ▼
                         ┌──────────────┐
                         │  EventBus    │
                         │  (Google     │
                         │   Guava)     │
                         └──────────────┘
```

**Key Characteristics:**
- **Models:** Domain entities in `mvp/model/` package
- **Views:** JavaFX components implementing view interfaces
- **Presenters:** Coordinate model updates and view rendering
- **EventBus:** Decoupled communication between components

### 3.3 Domain Model Structure

```
BMDProject (Container)
└── DoseResponseExperiment (Raw data)
    ├── PrefilterResults (Statistical filtering)
    │   ├── OneWayANOVAResults
    │   ├── WilliamsTrendResults
    │   ├── OriogenResults
    │   └── CurveFitPrefilterResults
    │
    └── BMDResult (Dose-response modeling)
        └── CategoryAnalysisResults (Pathway enrichment)
            └── IVIVEResult (PK modeling)
```

**Analysis Chain:** Each analysis maintains a reference to its parent, creating a traceable provenance chain.

### 3.4 Service Layer

| Service | Responsibility | Key Dependencies |
|---------|---------------|------------------|
| `BMDAnalysisService` | BMD curve fitting | BMDS, ToxicR, GCurveP |
| `PrefilterService` | Statistical prefiltering | Apache Commons Math, Sciome Math |
| `CategoryAnalysisService` | Pathway enrichment | Fisher's exact test, IVIVE |
| `ProjectNavigationService` | File I/O, serialization | Jackson, Java serialization |
| `BMDStatisticsService` | Statistical metrics | Apache Commons Math |

**Key Observation:** Services are well-abstracted with interfaces and minimal UI coupling. These can be reused directly in Spring.

### 3.5 UI Layer

**Framework:** JavaFX 17
**Layout:** FXML files in `/src/main/resources/fxml/`
**Charts:** JFreeChart 1.5.0
**Controls:** ControlsFX for enhanced UI widgets

**Main Views:**
- `MainView` - Primary application window
- `ProjectNavigationView` - Tree view of analyses
- `MainDataView` - Data table display
- `DataVisualizationView` - Charts and plots
- Analysis-specific dialogs (ANOVA, BMD, Category)

### 3.6 Command-Line Interface

**Entry Point:** `BMDExpressCommandLine.java`

**Capabilities:**
```bash
# Run complete analysis pipeline
bmdexpress3-cmd analyze --config-file pipeline.json

# Export results
bmdexpress3-cmd export --input-bm2 project.bm2 --output results.txt

# Query project contents
bmdexpress3-cmd query --input-bm2 project.bm2

# Combine projects
bmdexpress3-cmd combine --input-bm2-files p1.bm2 p2.bm2 --output merged.bm2
```

**Configuration:** JSON-based with full pipeline specification

**Significance:** Proves service layer works without UI, validating headless REST API approach.

### 3.7 Key Dependencies

**Core Libraries:**
- **JavaFX 17.0.13** - UI framework
- **Google Guava 33.3.1** - EventBus
- **JFreeChart 1.5.0** - Visualization
- **Jackson 2.18.1** - JSON serialization
- **Apache Commons Math3 3.6.1** - Statistics
- **Sciome Commons Math 1.04.0139** - Custom algorithms
- **ControlsFX 11.2.1** - Enhanced UI controls

**Native Integrations:**
- **BMDS** (EPA Benchmark Dose Software) - External process execution
- **ToxicR** (R package) - JNI integration for Bayesian model averaging

---

## 4. Web Application Architecture

### 4.1 Overall Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser)                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Vaadin UI (Server-side rendering)            │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │  │
│  │  │ Project │  │  Data   │  │Analysis │  │ Charts  │    │  │
│  │  │  Views  │  │ Import  │  │  Views  │  │  Views  │    │  │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│                              │ WebSocket (real-time updates)    │
│                              ▼                                   │
└─────────────────────────────────────────────────────────────────┘
                               │
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    Vaadin Views                            │ │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                 REST Controllers (API)                     │ │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                   Service Layer                            │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │ │
│  │  │   Analysis  │  │   Project   │  │     Job     │      │ │
│  │  │   Services  │  │   Services  │  │   Services  │      │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │ │
│  └───────────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                   Repository Layer                         │ │
│  │               (Spring Data JPA)                            │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                    │                  │                │
         ───────────┴──────────────────┴────────────────┴──────
                    ▼                  ▼                ▼
         ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐
         │   PostgreSQL     │  │    Redis     │  │  MinIO/S3    │
         │   (Metadata)     │  │ (Queue/Cache)│  │ (File Store) │
         └──────────────────┘  └──────────────┘  └──────────────┘

         ┌─────────────────────────────────────────────────────┐
         │              Worker Nodes (Async)                    │
         │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
         │  │ BMDS Docker  │  │ToxicR Docker │  │  Job     │  │
         │  │  Container   │  │  Container   │  │ Executor │  │
         │  └──────────────┘  └──────────────┘  └──────────┘  │
         └─────────────────────────────────────────────────────┘
```

### 4.2 Architectural Principles

1. **Separation of Concerns**: Clear boundaries between layers
2. **API-First**: REST endpoints for all operations
3. **Event-Driven**: Spring Events + WebSocket for real-time updates
4. **Async by Default**: Long-running analyses in background jobs
5. **Stateless Services**: Business logic independent of session state
6. **Multi-Tenancy**: User isolation at database and application levels

### 4.3 Key Architectural Changes from Desktop

| Aspect | Desktop | Web |
|--------|---------|-----|
| **State Management** | Single `BMDProject` in memory | Database-backed per-user projects |
| **Communication** | EventBus (in-process) | Spring Events + WebSocket |
| **UI Framework** | JavaFX | Vaadin |
| **Persistence** | Java serialization to .bm2 | PostgreSQL + JPA |
| **Processing** | Synchronous, single-threaded UI | Async jobs with progress tracking |
| **Scalability** | Single machine | Horizontal scaling with load balancer |
| **Authentication** | None | Spring Security with user accounts |

---

## 5. Migration Strategy

### 5.1 Component Reusability Assessment

```
┌─────────────────────────────────────────────────────────────────┐
│                    Reusability Matrix                            │
├──────────────────────┬──────────────┬───────────────────────────┤
│ Component            │ Reusability  │ Adaptation Required       │
├──────────────────────┼──────────────┼───────────────────────────┤
│ Service Layer        │ 95%          │ Minor: async patterns     │
│ Domain Models        │ 85%          │ Add JPA annotations       │
│ Business Logic       │ 100%         │ None                      │
│ Statistical Algos    │ 100%         │ None                      │
│ Utilities            │ 90%          │ Minor: file handling      │
│ CLI Configuration    │ 80%          │ Adapt to REST DTOs        │
│ UI Layer             │ 0%           │ Complete rewrite          │
│ EventBus             │ 0%           │ Replace with Spring Events│
│ File I/O             │ 50%          │ Add database, object store│
└──────────────────────┴──────────────┴───────────────────────────┘
```

**Overall Reusability: 60-70% of codebase**

### 5.2 Migration Approach

**Strategy: Incremental Port with Parallel Testing**

1. **Phase 1: Foundation** (Weeks 1-2)
   - Set up Spring Boot project structure
   - Configure database, Redis, object storage
   - Port domain models with JPA annotations
   - Create repository layer

2. **Phase 2: Service Layer** (Weeks 3-5)
   - Port analysis services
   - Implement job queue system
   - Add async execution with Spring `@Async`
   - Create progress tracking mechanism

3. **Phase 3: REST API** (Weeks 6-7)
   - Design DTOs and API contracts
   - Implement REST controllers
   - Add authentication/authorization
   - API testing and documentation

4. **Phase 4: Basic UI** (Weeks 8-11)
   - Main layout and navigation
   - Project management views
   - Data import/export
   - Analysis parameter forms
   - Results tables

5. **Phase 5: Advanced UI** (Weeks 12-15)
   - Interactive charts (Vaadin Charts or Plotly.js)
   - Real-time progress updates (WebSocket)
   - Advanced filtering and search
   - Data visualization tools

6. **Phase 6: Integration** (Weeks 16-17)
   - Docker containers for BMDS/ToxicR
   - Native library integration
   - End-to-end workflow testing
   - Performance optimization

7. **Phase 7: Migration & Polish** (Weeks 18-20)
   - Legacy .bm2 file import
   - User acceptance testing
   - Documentation
   - Deployment automation

### 5.3 Testing Strategy During Migration

- **Parallel Validation**: Run identical analyses in desktop and web versions
- **Golden Dataset**: Use reference datasets with known results
- **Regression Testing**: Automated tests for all statistical algorithms
- **Performance Benchmarking**: Compare execution times
- **User Acceptance**: Beta testing with actual researchers

---

## 6. Technology Stack

### 6.1 Backend Technologies

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.2.1 | Application framework |
| **Spring Data JPA** | 3.2.1 | Database access |
| **Spring Security** | 6.2.1 | Authentication/authorization |
| **Spring WebSocket** | 6.1.2 | Real-time updates |
| **Hibernate** | 6.4.1 | JPA implementation |
| **Maven** | 3.9+ | Build tool |
| **Lombok** | 1.18.30 | Boilerplate reduction |

### 6.2 Frontend Technologies

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Vaadin** | 24.3.3 | UI framework |
| **Vaadin Flow** | 24.3.3 | Server-side Java UI |
| **Vaadin Charts** | 24.3.3 | Interactive charts |
| **Vaadin Grid** | 24.3.3 | Data tables |

### 6.3 Data Storage

| Technology | Version | Purpose |
|-----------|---------|---------|
| **PostgreSQL** | 15+ | Primary database |
| **Redis** | 7+ | Job queue, caching, sessions |
| **MinIO** | Latest | Object storage (S3-compatible) |
| **Flyway** | 10+ | Database migrations |

### 6.4 Reused Libraries from Desktop

| Library | Version | Purpose |
|---------|---------|---------|
| **Apache Commons Math3** | 3.6.1 | Statistical computations |
| **Sciome Commons Math** | 1.04.0139 | Custom algorithms |
| **Jackson** | 2.18.1 | JSON serialization |
| **Apache Commons IO** | 2.17.0 | File utilities |
| **Apache Commons Lang3** | 3.17.0 | String/object utilities |
| **JAMA** | 1.0.3 | Matrix operations |
| **SLF4J + Logback** | 1.7.36 / 1.2.11 | Logging |

### 6.5 Deployment & DevOps

| Technology | Purpose |
|-----------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Local development |
| **Kubernetes** | Production orchestration (optional) |
| **Nginx** | Reverse proxy, load balancer |
| **Prometheus** | Metrics collection |
| **Grafana** | Monitoring dashboards |
| **GitHub Actions** | CI/CD pipelines |

### 6.6 Development Tools

| Tool | Purpose |
|------|---------|
| **IntelliJ IDEA** | IDE |
| **DBeaver** | Database client |
| **Postman** | API testing |
| **JUnit 5** | Unit testing |
| **Testcontainers** | Integration testing |
| **JMeter** | Performance testing |

---

## 7. Project Structure

### 7.1 Directory Layout

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

### 7.2 Package Organization

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

---

## 8. Implementation Phases

### Phase 1: Foundation (Weeks 1-2)

**Goal:** Establish project infrastructure and database schema

**Tasks:**
1. Create Spring Boot project with Maven
2. Configure PostgreSQL, Redis, MinIO via Docker Compose
3. Define JPA entities for core domain models
4. Create repository interfaces
5. Implement Flyway database migrations
6. Set up logging and monitoring
7. Configure Spring Security with basic authentication

**Deliverables:**
- Running Spring Boot application
- Database schema created
- Basic CRUD operations working
- Authentication system in place

**Success Criteria:**
- Can create/read/update/delete BMDProject entities
- Docker Compose brings up all infrastructure
- Basic REST endpoints respond

### Phase 2: Service Layer (Weeks 3-5)

**Goal:** Port and adapt core business logic

**Tasks:**
1. Port `BMDAnalysisService` from desktop
2. Port `PrefilterService` from desktop
3. Port `CategoryAnalysisService` from desktop
4. Implement `JobService` for async execution
5. Create job queue with Redis
6. Implement progress tracking mechanism
7. Add Spring `@Async` configuration
8. Port utility classes (BMDS integration, statistics)

**Deliverables:**
- All analysis services operational
- Job queue system working
- Progress updates functional

**Success Criteria:**
- Can execute ANOVA prefilter via service
- Can run BMD analysis asynchronously
- Job status tracked in database
- Progress updates available via API

### Phase 3: REST API (Weeks 6-7)

**Goal:** Expose services via REST endpoints

**Tasks:**
1. Design API contracts (request/response DTOs)
2. Implement `ProjectController`
3. Implement `AnalysisController`
4. Implement `JobController`
5. Implement `FileUploadController`
6. Add OpenAPI documentation (Springdoc)
7. Create Postman collection for testing
8. Implement error handling and validation

**Deliverables:**
- Complete REST API
- API documentation
- Postman test collection

**Success Criteria:**
- Can create project via API
- Can submit analysis jobs via API
- Can query job status via API
- Can download results via API
- API documentation auto-generated

### Phase 4: Basic UI (Weeks 8-11)

**Goal:** Create core Vaadin interface

**Tasks:**
1. Create `MainLayout` with navigation
2. Implement `ProjectListView`
3. Implement `ProjectNavigationView` (tree structure)
4. Create `ExpressionImportView` with file upload
5. Create analysis parameter forms:
   - `ANOVAAnalysisView`
   - `WilliamsTrendView`
   - `BMDAnalysisView`
   - `CategoryAnalysisView`
6. Create results tables:
   - `DoseResponseDataView`
   - `BMDResultsView`
   - `CategoryResultsView`
7. Implement job progress dialog
8. Add export functionality

**Deliverables:**
- Functional Vaadin UI
- Complete user workflow from import to export

**Success Criteria:**
- User can create project in UI
- User can import expression data
- User can configure and run analyses
- User can view results in tables
- User can export results

### Phase 5: Advanced UI (Weeks 12-15)

**Goal:** Add visualizations and real-time features

**Tasks:**
1. Integrate Vaadin Charts or Plotly.js
2. Implement dose-response curve charts
3. Implement BMD distribution plots (box plots, histograms)
4. Implement heatmaps for expression data
5. Add WebSocket support for real-time updates
6. Create interactive chart features (zoom, export)
7. Implement advanced filtering on result tables
8. Add search functionality across projects
9. Create user preferences/settings page

**Deliverables:**
- Interactive visualizations
- Real-time job progress updates
- Enhanced user experience

**Success Criteria:**
- Charts render correctly for all analysis types
- Real-time progress updates work without refresh
- Users can filter and search large result sets
- Chart exports (PNG, SVG) functional

### Phase 6: Integration (Weeks 16-17)

**Goal:** Integrate native dependencies and optimize

**Tasks:**
1. Create Docker image for BMDS execution
2. Create Docker image for ToxicR (R + rJava)
3. Implement process pool for BMDS
4. Implement Rserve connection pool for ToxicR
5. Add retry logic and error handling
6. Performance testing and optimization
7. Database query optimization
8. Implement caching strategies (Redis)
9. Add horizontal scaling support

**Deliverables:**
- Production-ready native integrations
- Optimized performance
- Scalability validated

**Success Criteria:**
- BMDS analyses execute in Docker containers
- ToxicR Bayesian model averaging works
- Can handle 10+ concurrent analyses
- Response times meet SLA (<5s interactive)
- Memory usage stable under load

### Phase 7: Migration & Polish (Weeks 18-20)

**Goal:** Production readiness and user onboarding

**Tasks:**
1. Implement .bm2 file import
2. Create migration scripts for legacy projects
3. User acceptance testing with researchers
4. Bug fixes and refinements
5. Write user documentation
6. Create video tutorials
7. Set up production deployment
8. Configure monitoring and alerting
9. Create backup/restore procedures
10. Security audit

**Deliverables:**
- Production deployment
- User documentation
- Migration tools

**Success Criteria:**
- Existing .bm2 projects import successfully
- Users can complete workflows without assistance
- System stable in production
- Monitoring dashboards operational
- Security vulnerabilities addressed

---

## 9. Key Components

### 9.1 Domain Layer

#### 9.1.1 BMDProject Entity

```java
@Entity
@Table(name = "bmd_projects")
public class BMDProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private String name;
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoseResponseExperiment> experiments;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<PrefilterResults> prefilterResults;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<BMDResult> bmdResults;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CategoryAnalysisResults> categoryResults;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Key Relationships:**
- **One-to-Many** with all analysis result types
- **Many-to-One** with User for multi-tenancy
- **Cascade operations** for child entities

#### 9.1.2 DoseResponseExperiment Entity

```java
@Entity
@Table(name = "dose_response_experiments")
public class DoseResponseExperiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private BMDProject project;

    private String name;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    private List<Treatment> treatments;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    private List<ProbeResponse> probeResponses;

    @ManyToOne
    @JoinColumn(name = "chip_info_id")
    private ChipInfo chip;

    @Enumerated(EnumType.STRING)
    private LogTransformationEnum logTransformation;

    // Large data stored in object storage
    @Column(name = "expression_matrix_s3_key")
    private String expressionMatrixS3Key;
}
```

**Design Decision:** Store large expression matrices in MinIO/S3, reference via key in database.

#### 9.1.3 BMDResult Entity

```java
@Entity
@Table(name = "bmd_results")
public class BMDResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private BMDProject project;

    @ManyToOne
    @JoinColumn(name = "experiment_id")
    private DoseResponseExperiment experiment;

    @ManyToOne
    @JoinColumn(name = "prefilter_id")
    private PrefilterResults prefilterResults;

    private String name;

    @Enumerated(EnumType.STRING)
    private BMDMethod bmdMethod; // BMDS, TOXICR_LAPLACE, TOXICR_MCMC, GCURVEP

    @OneToMany(mappedBy = "bmdResult", cascade = CascadeType.ALL)
    private List<ProbeStatResult> probeStatResults;

    @Column(name = "analysis_info", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private AnalysisInfo analysisInfo;

    // Large result set stored in object storage
    @Column(name = "results_s3_key")
    private String resultsS3Key;
}
```

**Design Decision:** Use JSONB column for analysis parameters, S3 for large result sets.

### 9.2 Service Layer

#### 9.2.1 BMDAnalysisService

**Interface:**
```java
public interface BMDAnalysisService {

    /**
     * Execute BMD analysis asynchronously
     * @return Job ID for tracking progress
     */
    String submitBMDAnalysis(
        Long projectId,
        Long dataId,
        BMDAnalysisRequest request,
        User user
    );

    /**
     * Execute BMD analysis synchronously (for testing)
     */
    BMDResult executeBMDAnalysis(
        IStatModelProcessable data,
        ModelInputParameters inputParams,
        ModelSelectionParameters modelSelectionParams,
        List<StatModel> modelsToRun
    );

    /**
     * Get analysis progress
     */
    JobProgress getAnalysisProgress(String jobId);
}
```

**Implementation Approach:**
1. Ported directly from desktop `BMDAnalysisService`
2. Added async wrapper methods
3. Progress callbacks publish Spring Events
4. Results saved to database + object storage

#### 9.2.2 JobService

**Interface:**
```java
public interface JobService {

    /**
     * Submit a job to the queue
     */
    String submitJob(JobRequest request, User user);

    /**
     * Get job status
     */
    JobStatus getJobStatus(String jobId);

    /**
     * Cancel a running job
     */
    void cancelJob(String jobId);

    /**
     * Get all jobs for a user
     */
    List<JobStatus> getUserJobs(Long userId);

    /**
     * Publish progress update
     */
    void publishProgress(String jobId, double progress, String message);
}
```

**Job Queue Implementation:**
- **Redis-based queue** using Spring Data Redis
- **Job entity** persisted in PostgreSQL
- **Status updates** published via WebSocket
- **Retry logic** for transient failures

### 9.3 REST API Layer

#### 9.3.1 AnalysisController

```java
@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    @PostMapping("/bmd")
    public ResponseEntity<JobSubmissionResponse> submitBMDAnalysis(
        @RequestBody BMDAnalysisRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String jobId = analysisService.submitBMDAnalysis(
            request.getProjectId(),
            request.getDataId(),
            request,
            getCurrentUser(userDetails)
        );
        return ResponseEntity.accepted()
            .body(new JobSubmissionResponse(jobId));
    }

    @PostMapping("/prefilter/anova")
    public ResponseEntity<JobSubmissionResponse> submitANOVA(
        @RequestBody ANOVARequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Similar pattern
    }

    @GetMapping("/{analysisId}/results")
    public ResponseEntity<BMDResultDTO> getResults(
        @PathVariable Long analysisId
    ) {
        // Return analysis results
    }
}
```

**API Design Principles:**
- **Async by default**: Return job ID immediately
- **RESTful resources**: `/api/v1/{resource}/{id}`
- **Hypermedia**: Include links to related resources
- **Versioning**: `/v1/` prefix for API evolution

### 9.4 Vaadin UI Layer

#### 9.4.1 MainLayout

```java
@Route("")
@PageTitle("BMDExpress Web")
public class MainLayout extends AppLayout {

    private final ProjectService projectService;

    public MainLayout(ProjectService projectService) {
        this.projectService = projectService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("BMDExpress Web");
        logo.addClassName("logo");

        Button logout = new Button("Logout", e -> logout());

        HorizontalLayout header = new HorizontalLayout(logo, logout);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink projectsLink = new RouterLink("Projects", ProjectListView.class);
        RouterLink dataLink = new RouterLink("Data Import", DataImportView.class);
        RouterLink analysisLink = new RouterLink("Analysis", AnalysisView.class);

        addToDrawer(new VerticalLayout(
            projectsLink,
            dataLink,
            analysisLink
        ));
    }
}
```

#### 9.4.2 BMDAnalysisView

```java
@Route(value = "analysis/bmd", layout = MainLayout.class)
@PageTitle("BMD Analysis")
public class BMDAnalysisView extends VerticalLayout {

    private final BMDAnalysisService analysisService;
    private final JobService jobService;

    private ComboBox<IStatModelProcessable> dataSelector;
    private CheckBoxGroup<StatModel> modelSelector;
    private NumberField bmrField;
    private ComboBox<BMDMethod> methodSelector;
    private Button runButton;

    public BMDAnalysisView(
        BMDAnalysisService analysisService,
        JobService jobService
    ) {
        this.analysisService = analysisService;
        this.jobService = jobService;

        createUI();
    }

    private void createUI() {
        H2 title = new H2("BMD Analysis");

        // Data selection
        dataSelector = new ComboBox<>("Select Data");
        dataSelector.setItems(getAvailableData());
        dataSelector.setItemLabelGenerator(IStatModelProcessable::getName);

        // Model selection
        modelSelector = new CheckBoxGroup<>("Models to Run");
        modelSelector.setItems(StatModel.values());

        // BMR configuration
        bmrField = new NumberField("Benchmark Response (BMR)");
        bmrField.setValue(1.349);
        bmrField.setStep(0.1);

        // Method selection
        methodSelector = new ComboBox<>("Method");
        methodSelector.setItems(BMDMethod.values());
        methodSelector.setValue(BMDMethod.BMDS);

        // Run button
        runButton = new Button("Run Analysis", e -> runAnalysis());
        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Layout
        FormLayout form = new FormLayout();
        form.add(dataSelector, modelSelector, bmrField, methodSelector);

        add(title, form, runButton);
    }

    private void runAnalysis() {
        BMDAnalysisRequest request = new BMDAnalysisRequest();
        request.setDataId(dataSelector.getValue().getId());
        request.setModels(modelSelector.getSelectedItems());
        request.setBmr(bmrField.getValue());
        request.setMethod(methodSelector.getValue());

        String jobId = analysisService.submitBMDAnalysis(
            getCurrentProjectId(),
            request.getDataId(),
            request,
            getCurrentUser()
        );

        // Show progress dialog
        AnalysisProgressDialog dialog = new AnalysisProgressDialog(jobId, jobService);
        dialog.open();
    }
}
```

#### 9.4.3 Real-Time Progress Updates

```java
@Push
@Route(value = "analysis/progress", layout = MainLayout.class)
public class AnalysisProgressView extends VerticalLayout {

    private final JobService jobService;
    private final UI ui;

    private ProgressBar progressBar;
    private Span statusLabel;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public AnalysisProgressView(JobService jobService) {
        this.jobService = jobService;
        this.ui = UI.getCurrent();

        createUI();
        subscribeToProgress();
    }

    @EventListener
    public void onJobProgress(JobProgressEvent event) {
        // Update UI from background thread
        ui.access(() -> {
            progressBar.setValue(event.getProgress());
            statusLabel.setText(event.getMessage());
        });
    }
}
```

**Key Feature:** Use Vaadin `@Push` annotation for server-to-client updates via WebSocket.

---

## 10. Data Model

### 10.1 Database Schema Overview

```
┌─────────────────┐
│     users       │
└────────┬────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐
│  bmd_projects   │
└────────┬────────┘
         │
         │ 1:N
         ├──────────────────┬──────────────────┬──────────────────┐
         ▼                  ▼                  ▼                  ▼
┌──────────────────┐ ┌───────────────┐ ┌──────────────┐ ┌──────────────┐
│dose_response_exp │ │prefilter_res  │ │  bmd_results │ │category_res  │
└──────────────────┘ └───────────────┘ └──────────────┘ └──────────────┘
         │                  │                  │                  │
         │ 1:N              │ 1:N              │ 1:N              │ 1:N
         ▼                  ▼                  ▼                  ▼
┌──────────────────┐ ┌───────────────┐ ┌──────────────┐ ┌──────────────┐
│   treatments     │ │prefilter_res  │ │probe_stat_res│ │category_res  │
│  probe_responses │ │   (detail)    │ │  (detail)    │ │   (detail)   │
└──────────────────┘ └───────────────┘ └──────────────┘ └──────────────┘
```

### 10.2 Core Tables

#### users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

#### bmd_projects
```sql
CREATE TABLE bmd_projects (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_user ON bmd_projects(user_id);
CREATE INDEX idx_projects_created ON bmd_projects(created_at DESC);
```

#### dose_response_experiments
```sql
CREATE TABLE dose_response_experiments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES bmd_projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    chip_info_id BIGINT REFERENCES chip_info(id),
    log_transformation VARCHAR(50),
    expression_matrix_s3_key VARCHAR(512),
    num_probes INTEGER,
    num_treatments INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_experiments_project ON dose_response_experiments(project_id);
```

#### bmd_results
```sql
CREATE TABLE bmd_results (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES bmd_projects(id) ON DELETE CASCADE,
    experiment_id BIGINT REFERENCES dose_response_experiments(id),
    prefilter_id BIGINT REFERENCES prefilter_results(id),
    name VARCHAR(255) NOT NULL,
    bmd_method VARCHAR(50) NOT NULL,
    analysis_info JSONB,
    results_s3_key VARCHAR(512),
    num_results INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bmd_results_project ON bmd_results(project_id);
CREATE INDEX idx_bmd_results_experiment ON bmd_results(experiment_id);
CREATE INDEX idx_bmd_results_prefilter ON bmd_results(prefilter_id);
CREATE INDEX idx_bmd_results_method ON bmd_results(bmd_method);
```

#### jobs
```sql
CREATE TABLE jobs (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    project_id BIGINT REFERENCES bmd_projects(id),
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress DOUBLE PRECISION DEFAULT 0.0,
    message TEXT,
    parameters JSONB,
    result_id BIGINT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_jobs_user ON jobs(user_id);
CREATE INDEX idx_jobs_project ON jobs(project_id);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_created ON jobs(created_at DESC);
```

### 10.3 Storage Strategy

**Hybrid Approach: Database + Object Storage**

| Data Type | Storage | Rationale |
|-----------|---------|-----------|
| **Metadata** | PostgreSQL | Fast queries, relationships |
| **Small results** (<1MB) | PostgreSQL JSONB | Single-query retrieval |
| **Large results** (>1MB) | MinIO/S3 | Cost-effective, scalable |
| **Expression matrices** | MinIO/S3 | Large, rarely queried in full |
| **Charts/exports** | MinIO/S3 | Binary files |

**Object Storage Naming Convention:**
```
projects/{projectId}/experiments/{experimentId}/matrix.bin
projects/{projectId}/results/{resultId}/probe_stats.json.gz
projects/{projectId}/exports/{exportId}/results.csv
```

---

## 11. API Design

### 11.1 API Structure

**Base URL:** `https://bmdexpress.sciome.com/api/v1`

**Authentication:** JWT Bearer tokens

### 11.2 Core Endpoints

#### Projects

```
GET    /api/v1/projects              # List user's projects
POST   /api/v1/projects              # Create project
GET    /api/v1/projects/{id}         # Get project details
PUT    /api/v1/projects/{id}         # Update project
DELETE /api/v1/projects/{id}         # Delete project
```

#### Data Import

```
POST   /api/v1/projects/{id}/experiments     # Import expression data
GET    /api/v1/experiments/{id}              # Get experiment details
DELETE /api/v1/experiments/{id}              # Delete experiment
```

#### Analysis Submission

```
POST   /api/v1/analysis/prefilter/anova      # Submit ANOVA
POST   /api/v1/analysis/prefilter/williams   # Submit Williams trend
POST   /api/v1/analysis/bmd                  # Submit BMD analysis
POST   /api/v1/analysis/category             # Submit category analysis
```

#### Job Management

```
GET    /api/v1/jobs                          # List user's jobs
GET    /api/v1/jobs/{jobId}                  # Get job status
DELETE /api/v1/jobs/{jobId}                  # Cancel job
GET    /api/v1/jobs/{jobId}/logs             # Get job logs
```

#### Results Retrieval

```
GET    /api/v1/results/bmd/{id}              # Get BMD results
GET    /api/v1/results/category/{id}         # Get category results
GET    /api/v1/results/{id}/export           # Export results
```

### 11.3 Example API Requests

#### Submit BMD Analysis

```http
POST /api/v1/analysis/bmd
Authorization: Bearer {token}
Content-Type: application/json

{
  "projectId": 123,
  "experimentId": 456,
  "prefilterId": 789,
  "name": "BMD Analysis - Hill, Power",
  "method": "BMDS",
  "models": ["HILL", "POWER", "EXPONENTIAL_2", "EXPONENTIAL_3"],
  "inputParameters": {
    "bmr": 1.349,
    "constantVariance": true,
    "restrictPower": true,
    "confidence": 0.95,
    "iterations": 250
  },
  "modelSelection": {
    "flagHillWithKLessThanOneFourth": true,
    "useNestedChiSquared": true,
    "pValueCutoff": 0.05
  }
}
```

**Response:**
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "QUEUED",
  "message": "Analysis job submitted successfully",
  "estimatedDuration": "15 minutes",
  "_links": {
    "self": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "cancel": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }
}
```

#### Get Job Status

```http
GET /api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Authorization: Bearer {token}
```

**Response:**
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "jobType": "BMD_ANALYSIS",
  "status": "RUNNING",
  "progress": 0.45,
  "message": "Processing gene 2250 of 5000",
  "createdAt": "2025-10-15T10:00:00Z",
  "startedAt": "2025-10-15T10:00:15Z",
  "estimatedCompletion": "2025-10-15T10:12:00Z",
  "_links": {
    "self": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "cancel": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "logs": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890/logs"
  }
}
```

### 11.4 WebSocket API

**Endpoint:** `wss://bmdexpress.sciome.com/ws`

**Subscribe to job updates:**
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  stompClient.subscribe('/user/queue/job-progress', function(message) {
    const progress = JSON.parse(message.body);
    updateProgressBar(progress.progress);
    updateStatusMessage(progress.message);
  });
});
```

---

## 12. Security Considerations

### 12.1 Authentication

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

### 12.2 Authorization

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

### 12.3 Data Isolation

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

### 12.4 Input Validation

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

### 12.5 Rate Limiting

**Strategy:** Redis-backed rate limiter

**Limits:**
- 100 API requests per minute per user
- 10 analysis jobs per hour per user
- 5 concurrent jobs per user

### 12.6 Security Checklist

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

---

## 13. Deployment Architecture

### 13.1 Production Architecture

```
                          ┌─────────────────┐
                          │   Load Balancer │
                          │     (Nginx)     │
                          └────────┬────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
         ┌──────────▼─────────┐       ┌──────────▼─────────┐
         │  Web Server 1      │       │  Web Server 2      │
         │  (Spring Boot +    │       │  (Spring Boot +    │
         │   Vaadin)          │       │   Vaadin)          │
         └──────────┬─────────┘       └──────────┬─────────┘
                    │                             │
                    └──────────────┬──────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
    ┌─────▼─────┐          ┌──────▼──────┐        ┌───────▼──────┐
    │PostgreSQL │          │    Redis    │        │   MinIO/S3   │
    │ (Primary) │          │(Queue/Cache)│        │(Object Store)│
    └───────────┘          └─────────────┘        └──────────────┘
          │
    ┌─────▼─────┐
    │PostgreSQL │
    │ (Replica) │
    └───────────┘

         ┌──────────────────────────────────────────────┐
         │         Worker Nodes (Kubernetes)            │
         │  ┌─────────────┐  ┌─────────────┐  ┌─────┐  │
         │  │BMDS Worker 1│  │BMDS Worker 2│  │ ... │  │
         │  └─────────────┘  └─────────────┘  └─────┘  │
         │  ┌─────────────┐  ┌─────────────┐  ┌─────┐  │
         │  │ToxicR Wrkr 1│  │ToxicR Wrkr 2│  │ ... │  │
         │  └─────────────┘  └─────────────┘  └─────┘  │
         └──────────────────────────────────────────────┘
```

### 13.2 Docker Compose (Development)

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: bmdexpress
      POSTGRES_USER: bmduser
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio-data:/data

  bmdexpress-web:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bmdexpress
      SPRING_DATASOURCE_USERNAME: bmduser
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      MINIO_ENDPOINT: http://minio:9000
    depends_on:
      - postgres
      - redis
      - minio

volumes:
  postgres-data:
  minio-data:
```

### 13.3 Kubernetes Deployment (Production)

**Key Resources:**

**Web Application Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bmdexpress-web
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bmdexpress-web
  template:
    metadata:
      labels:
        app: bmdexpress-web
    spec:
      containers:
      - name: bmdexpress-web
        image: sciome/bmdexpress-web:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

**BMDS Worker Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bmds-worker
spec:
  replicas: 5
  selector:
    matchLabels:
      app: bmds-worker
  template:
    metadata:
      labels:
        app: bmds-worker
    spec:
      containers:
      - name: bmds-worker
        image: sciome/bmds-worker:latest
        env:
        - name: REDIS_HOST
          value: redis-service
        resources:
          requests:
            memory: "4Gi"
            cpu: "2000m"
          limits:
            memory: "8Gi"
            cpu: "4000m"
```

### 13.4 Monitoring & Observability

**Metrics Collection:**
- **Prometheus** for metrics scraping
- **Micrometer** (Spring Boot Actuator) for metrics exposure
- **Grafana** dashboards for visualization

**Key Metrics:**
- Request rate, latency, error rate
- Job queue depth, job completion rate
- Database connection pool metrics
- JVM memory, garbage collection
- BMDS/ToxicR process metrics

**Logging:**
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- Structured JSON logs
- Correlation IDs for request tracing

**Alerting:**
- **Alertmanager** (Prometheus)
- Slack/email notifications
- Alert rules:
  - High error rate (>5%)
  - Job queue backup (>100 pending)
  - Database connection saturation
  - High memory usage (>90%)

---

## 14. Testing Strategy

### 14.1 Testing Pyramid

```
                    ┌───────────────┐
                    │  E2E Tests    │  (5%)
                    │  (Selenium)   │
                    └───────────────┘
                 ┌─────────────────────┐
                 │ Integration Tests   │  (20%)
                 │  (Testcontainers)   │
                 └─────────────────────┘
          ┌──────────────────────────────────┐
          │        Unit Tests                │  (75%)
          │   (JUnit, Mockito)               │
          └──────────────────────────────────┘
```

### 14.2 Unit Testing

**Scope:** Individual classes and methods

**Tools:**
- JUnit 5
- Mockito for mocking
- AssertJ for fluent assertions

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class BMDAnalysisServiceTest {

    @Mock
    private BMDSTool bmdsTool;

    @Mock
    private BMDResultRepository resultRepository;

    @InjectMocks
    private BMDAnalysisService analysisService;

    @Test
    void testExecuteBMDAnalysis() {
        // Given
        DoseResponseExperiment experiment = createTestExperiment();
        ModelInputParameters params = createTestParams();

        // When
        BMDResult result = analysisService.executeBMDAnalysis(
            experiment, params, modelSelectionParams, models
        );

        // Then
        assertThat(result.getProbeStatResults()).hasSize(10);
        assertThat(result.getBmdMethod()).isEqualTo(BMDMethod.BMDS);
    }
}
```

### 14.3 Integration Testing

**Scope:** Component interactions with real infrastructure

**Tools:**
- Spring Boot Test
- Testcontainers (PostgreSQL, Redis)
- MockMvc for API testing

**Example:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class AnalysisControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testSubmitBMDAnalysis() {
        // Given
        BMDAnalysisRequest request = new BMDAnalysisRequest();
        request.setProjectId(1L);
        request.setExperimentId(1L);

        // When
        ResponseEntity<JobSubmissionResponse> response = restTemplate
            .postForEntity("/api/v1/analysis/bmd", request, JobSubmissionResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getJobId()).isNotNull();
    }
}
```

### 14.4 End-to-End Testing

**Scope:** Complete user workflows

**Tools:**
- Selenium WebDriver
- Vaadin TestBench

**Test Scenarios:**
1. User registration and login
2. Create project
3. Import expression data
4. Run ANOVA prefilter
5. Run BMD analysis
6. View results
7. Export results

### 14.5 Performance Testing

**Tools:**
- Apache JMeter
- Gatling

**Scenarios:**
- 100 concurrent users browsing
- 50 concurrent analysis jobs
- Large file upload (100MB expression data)

**Success Criteria:**
- 95th percentile response time <5s
- Job throughput: 20 jobs/minute
- No memory leaks under sustained load

---

## 15. Risk Mitigation

### 15.1 Technical Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| **Native library integration issues** | Medium | High | Early prototype in Phase 6; Docker isolation; fallback to REST API for ToxicR |
| **Performance degradation** | Medium | High | Early performance testing; profiling; caching; horizontal scaling |
| **Data migration issues** | Medium | Medium | Extensive testing with real .bm2 files; validation scripts; rollback plan |
| **UI complexity** | Low | Medium | Iterative UI development; user feedback; simplified initial release |
| **Database scalability** | Low | High | Proper indexing; read replicas; partitioning strategy; object storage for large data |

### 15.2 Schedule Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| **Underestimated development time** | High | Medium | Buffer time in schedule; prioritize MVP features; agile sprints |
| **Dependency on external libraries** | Low | High | Early validation of key dependencies; vendor evaluation |
| **Testing bottlenecks** | Medium | Medium | Automated testing; continuous integration; parallel testing |

### 15.3 Operational Risks

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| **Production deployment issues** | Medium | High | Staging environment; blue-green deployment; rollback automation |
| **Security vulnerabilities** | Medium | High | Security audit; dependency scanning; penetration testing |
| **User adoption challenges** | Medium | Medium | User training; documentation; support channels; phased rollout |

### 15.4 Contingency Plans

**If native libraries fail to integrate:**
- Option 1: Use REST APIs to external services
- Option 2: Provide desktop CLI as alternative
- Option 3: Partner with cloud providers for hosted solutions

**If performance targets not met:**
- Implement aggressive caching
- Add more worker nodes
- Optimize database queries
- Consider batch processing only

**If schedule slips:**
- Release MVP with core features only
- Defer advanced visualizations
- Focus on API first, UI second

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| **BMD** | Benchmark Dose - dose associated with a specified change in response |
| **BMDL** | Benchmark Dose Lower Confidence Limit |
| **BMDU** | Benchmark Dose Upper Confidence Limit |
| **BMR** | Benchmark Response - predetermined level of response |
| **BMDS** | Benchmark Dose Software (EPA) |
| **ToxicR** | R package for Bayesian BMD modeling |
| **IVIVE** | In Vitro to In Vivo Extrapolation |
| **HTTK** | High-Throughput Toxicokinetics |
| **GCurveP** | Non-parametric dose-response modeling method |
| **ANOVA** | Analysis of Variance |
| **Williams Trend** | Statistical test for dose-response trend |
| **Oriogen** | Statistical prefilter method |
| **Curve Fit Prefilter** | Identifies dose-responsive genes via curve fitting |

## Appendix B: References

1. BMDExpress 2.0 Paper: [PMC6513160](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6513160/)
2. ToxicR Package: [NIEHS/ToxicR GitHub](https://github.com/NIEHS/ToxicR)
3. ToxicR Statistical Methods: [PMC9799099](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9799099/)
4. EPA BMDS: [EPA Website](https://www.epa.gov/bmds)
5. Spring Boot Documentation: [spring.io](https://spring.io/projects/spring-boot)
6. Vaadin Documentation: [vaadin.com/docs](https://vaadin.com/docs/latest)

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2025-10-15 | Dan Svoboda, Claude Code | Initial draft |

---

**End of Design Document**
