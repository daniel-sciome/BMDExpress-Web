# Implementation Phases

## Phase 1: Foundation (Weeks 1-2)

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

## Phase 2: Service Layer (Weeks 3-5)

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

## Phase 3: REST API (Weeks 6-7)

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

## Phase 4: Basic UI (Weeks 8-11)

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

## Phase 5: Advanced UI (Weeks 12-15)

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

## Phase 6: Integration (Weeks 16-17)

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

## Phase 7: Migration & Polish (Weeks 18-20)

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
