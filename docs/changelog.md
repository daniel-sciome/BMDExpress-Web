# Changelog

All notable changes to the BMDExpress-Web project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

**2025-10-16 19:55** - Added stubbed category analysis functionality from prototype

Implemented complete API surface for category analysis to mirror `/tmp/server` prototype functionality. All endpoints are properly stubbed with clear documentation referencing the prototype for future implementation.

- **CategoryAnalysisParametersDto**: Comprehensive DTO with all analysis parameters
  - BMD filtering parameters (p-value, BMD/BMDL ratios, r-squared cutoffs)
  - Gene set size filters (min/max genes in set)
  - Fold change and prefilter settings
  - GO-specific parameters (category selection)
  - Pathway-specific parameters (database selection)
  - Defined category file parameters (probe/category file paths)
  - Total of 20+ configurable parameters matching desktop app

- **Export Endpoints** (STUBBED):
  - `GET /api/category-analysis/{analysisId}/export?format=json` - Export as JSON
  - `GET /api/category-analysis/{analysisId}/export?format=tsv` - Export as TSV
  - Endpoints return placeholder data with clear stub warnings
  - See `/tmp/server/controller/CategoryAnalysisController.java` lines 135-201 for full implementation

- **CategoryAnalysisAsyncService** (STUBBED):
  - Added comprehensive documentation of what needs to be implemented
  - Stub method `convertToParameters()` documents 140 lines of parameter conversion logic
  - References prototype implementation at `/tmp/server/service/CategoryAnalysisAsyncService.java:100-242`
  - Logs warnings when stubbed methods are called

- **Updated CategoryAnalysisRequest**: Changed from generic `Map<String, Object>` to typed `CategoryAnalysisParametersDto`
- **Updated CategoryAnalysisControllerTest**: All 7 tests updated to use new DTO structure

**Stub Status Documentation**:
- All stubs clearly marked with "STUB:" comments
- Each stub references corresponding prototype implementation location
- Warns via logging when stubbed functionality is executed
- Infrastructure ready for implementation - only business logic needs completion

**Test Suite**: All 64 tests passing (58 unit + 6 integration)

**2025-10-16 18:55** - Completed REST API with symmetric endpoints and comprehensive testing

Implemented missing REST API endpoints to provide complete, symmetric access to BMD and category analysis results:
- **New REST Endpoints**:
  - `GET /api/projects/{projectId}/bmd-results/{resultName}` - Retrieve specific BMD result with full data
  - `GET /api/projects/{projectId}/category-results` - List all category result names in project
  - These complete the API symmetry alongside existing endpoints

- **Integration Tests with Real Data**: Created comprehensive integration test suite
  - New test class: `ProjectApiIntegrationTest` (6 tests)
  - Tests full stack with real 35MB .bm2 file (P3MP-Parham.bm2)
  - Verifies: file upload → deserialization → querying → JSON serialization → deletion
  - Validates API works with actual BMDExpress data (21 BMD results, 42 category analyses)
  - Test execution: ~40 seconds for complete integration suite

- **File Upload Configuration**: Increased limits for large .bm2 files
  - Set `spring.servlet.multipart.max-file-size=100MB`
  - Set `spring.servlet.multipart.max-request-size=100MB`
  - Configured in both `application.properties` and `application-test.properties`
  - Enables upload of typical BMDExpress project files (10-50MB)

- **Live API Verification Script**: Created `/tmp/test_api.sh` for end-to-end testing
  - Tests real HTTP requests with curl
  - Verifies complete workflow: upload 35MB file → query all endpoints → cleanup
  - All 6 test scenarios pass with real data

**Test Coverage Summary**:
- **Unit tests**: 58 tests (controller logic with mocks)
  - ProjectControllerTest: 18 tests (added 2 new for missing endpoints)
  - CategoryAnalysisControllerTest: 7 tests
  - GlobalExceptionHandlerTest: 7 tests
  - ProjectServiceTest: 14 tests
  - BmdResultsServiceTest: 6 tests
  - CategoryResultsServiceTest: 6 tests
- **Integration tests**: 6 tests (full stack with real 35MB .bm2 file)
- **Live API tests**: 6 manual verification scenarios
- **Total: 64 automated tests, all passing**

**Complete REST API Surface** (ready for UI consumption):
```
POST   /api/projects                                        - Upload .bm2 file
GET    /api/projects/{projectId}                           - Get project metadata
GET    /api/projects/{projectId}/full                      - Get complete project object
GET    /api/projects/{projectId}/bmd-results               - List BMD result names
GET    /api/projects/{projectId}/bmd-results/{resultName}  - Get specific BMD result ✨ NEW
GET    /api/projects/{projectId}/category-results          - List category result names ✨ NEW
GET    /api/projects/{projectId}/category-results/{name}   - Get specific category result
DELETE /api/projects/{projectId}                           - Delete project
GET    /api/projects/available-files                       - List .bm2 files on server
POST   /api/projects/load-from-file                        - Load .bm2 from server filesystem
```

**Implementation Approach**:
- Followed TDD (Test-Driven Development) methodology
- Red-Green-Refactor cycle for all new endpoints
- Added error handling for incomplete BMDResult mock data
- Maintained consistent API patterns across all endpoints

**2025-10-15 17:45** - Implemented REST API for .bm2 project management
- Created comprehensive REST API infrastructure following TDD methodology
  - **ProjectController** - RESTful endpoints for project operations
    - `POST /api/projects` - Upload .bm2 file (multipart/form-data)
    - `GET /api/projects/{projectId}` - Get project metadata
    - `GET /api/projects/{projectId}/full` - Get complete BMDProject object
    - `GET /api/projects/{projectId}/bmd-results` - List BMD result names
    - `GET /api/projects/{projectId}/category-results/{resultName}` - Get specific category result
    - `GET /api/projects/available-files` - List .bm2 files in server directory
    - `POST /api/projects/load-from-file` - Load .bm2 from server filesystem
    - `DELETE /api/projects/{projectId}` - Delete project from memory
  - **GlobalExceptionHandler** - Centralized exception handling with @ControllerAdvice
    - Context-aware HTTP status codes (400 Bad Request, 404 Not Found, 500 Internal Server Error)
    - Distinguishes "not found" vs "bad request" via message content analysis
    - Handles IllegalArgumentException, RuntimeException, IOException, ClassNotFoundException
    - Returns structured ErrorResponse JSON with status, error type, message, timestamp, and path
  - **DTOs** - Data Transfer Objects for API responses
    - `ProjectUploadResponse` - Project metadata with BMD/category result names
    - `ErrorResponse` - Standardized error response format
- Created specialized service layer with clear separation of concerns
  - **ProjectService** (renamed from ProjectManagementService)
    - Project lifecycle management (load, store, retrieve, delete)
    - In-memory storage using ConcurrentHashMap
    - Project metadata tracking (UUID, filename, timestamp)
  - **BmdResultsService** - BMD analysis result queries
    - Case-insensitive result name lookup
    - Result name enumeration
  - **CategoryResultsService** - Category analysis result queries
    - Case-insensitive result name lookup
    - Result name enumeration
- Comprehensive test suite with 47 passing tests
  - **ProjectControllerTest** (14 tests) - Controller integration tests with MockMvc
  - **GlobalExceptionHandlerTest** (7 tests) - Exception handling verification
  - **ProjectServiceTest** (14 tests) - Service layer unit tests
  - **BmdResultsServiceTest** (6 tests) - BMD result service tests
  - **CategoryResultsServiceTest** (6 tests) - Category result service tests
- Security features
  - Directory traversal prevention in file loading endpoints
  - Filename validation (reject paths with `..`, `/`, `\`)
  - File existence and readability checks

**2025-10-15 16:00** - Implemented .bm2 file support (design deviation)
- Added BMDExpress3 desktop application JAR as Maven dependency
  - Installed `bmdexpress3-3.0.0-SNAPSHOT.jar` to local Maven repository
  - Provides access to `BMDProject`, `BMDResult`, and `BMDExpressProperties` classes
- Created `ProjectService` for .bm2 file management (initially named ProjectManagementService)
  - Deserializes .bm2 files using Java `ObjectInputStream`
  - Stores projects in-memory using `ConcurrentHashMap<String, ProjectHolder>`
  - Tracks project metadata (UUID, filename, upload timestamp)
- Adapted existing prototype code from `/tmp/server` to project architecture
  - Updated package structure from `com.sciome.bmdexpress2.server` to `com.sciome.bmdexpressweb`
  - Preserved functionality while integrating with Spring Boot configuration
- Created `data/projects/` directory for .bm2 file storage
- Added comprehensive documentation in Appendix A: .bm2 File Implementation
  - Explains design deviation from database-first approach
  - Documents BMDExpress JAR dependency integration
  - Describes code adaptation strategy
  - Outlines future database integration path
- Updated MkDocs navigation to include Appendices section

**2025-10-15 09:30** - Restarted server after DevTools instability
- Clean server restart to resolve crash from rapid DevTools reloads
- Verified MainView component is properly compiled and accessible

**2025-10-15 09:18** - Fixed documentation browser caching issues
- Added cache-control headers to `DocsFilter` to prevent browser caching
  - `Cache-Control: no-cache, no-store, must-revalidate`
  - `Pragma: no-cache`
  - `Expires: 0`
- Documentation changes now appear immediately without hard refresh

**2025-10-15 09:15** - Fixed ASCII diagram alignment in architecture documentation
- Corrected pipe symbol alignment in Web Architecture diagram
- All diagram lines now have consistent width (65 characters)
- Rebuilt MkDocs documentation with updated content

**2025-10-15 09:08** - Implemented documentation serving system
- Created custom `DocsFilter` servlet filter for serving documentation at `/docs/` path
  - Configured with `@Order(Ordered.HIGHEST_PRECEDENCE)` to run before Vaadin routing
  - Handles content type detection for various file types (HTML, CSS, JS, images, fonts)
  - Serves static files from classpath resources
- Added `WebConfig` Spring MVC configuration
  - Resource handlers for `/docs/**` paths
  - View controller redirects for `/docs` to `/docs/index.html`
- Resolved Vaadin routing conflicts that were causing "Could not navigate to 'docs'" errors

**2025-10-15 08:45** - Auto-compilation and hot reload workflow
- Created `watch-compile.sh` script for automatic compilation on file changes
  - Monitors `src/main/java` for changes using file timestamps
  - Automatically runs `mvn compile` when Java files are modified
  - Triggers Spring Boot DevTools reload within 2-3 seconds
- Resolved development bottleneck with manual compilation requirement

**2025-10-15 08:30** - Initial documentation setup
- Configured MkDocs with Material theme
  - Light/dark mode support
  - Navigation tabs and sections
  - Git revision dates plugin
  - Mermaid diagram support
  - Code syntax highlighting
  - Search functionality
- Created comprehensive design documentation:
  - Overview and background
  - Desktop application analysis
  - Web architecture design
  - Migration strategy
  - Technology stack overview
  - Implementation phases
  - Data model design
  - API design specifications
  - Security considerations
  - Deployment strategy
  - Testing approach
  - Risk mitigation
  - Glossary
- Symlinked MkDocs build output (`site/`) to Spring Boot static resources

**2025-10-15 08:00** - Main application view
- Created `MainView` Vaadin component as application landing page
  - Title and description
  - Documentation link button
  - Demo greeting functionality with text field
- Configured with `@Route("")` to handle root path

### Changed

**2025-10-15 17:45** - Service layer architectural refactoring
- Renamed `ProjectManagementService` to `ProjectService` following Spring naming conventions
  - "Service" is architectural designation, not operation count
  - Aligns with Spring Boot best practices (singular, domain-based naming)
- Split `AnalysisResultsService` into specialized services to prevent "god class" anti-pattern
  - Created `BmdResultsService` for BMD-specific operations
  - Created `CategoryResultsService` for category analysis operations
  - Improves maintainability and follows Single Responsibility Principle
  - Enables future expansion for additional analysis types (PrefilterResultsService, IviveResultsService, etc.)
- Updated `ProjectController` to inject specialized services instead of umbrella service
- All 47 tests passing after refactoring

**2025-10-15 09:18** - Documentation serving approach
- Switched from Spring MVC resource handlers to custom servlet filter
- Removed problematic VaadinConfig and VaadinServletConfiguration files that caused server crashes

### Fixed

**2025-10-15 09:30** - Server stability
- Resolved server crash (exit code 144) caused by rapid DevTools restarts

**2025-10-15 09:18** - Browser caching preventing documentation updates
- Documentation changes now visible immediately without hard refresh

**2025-10-15 09:08** - Vaadin routing conflicts
- Fixed "Could not navigate to 'docs/design/overview'" errors
- Documentation links now work correctly in browser

## [0.1.0] - 2025-10-15

### Added

**2025-10-15 07:00** - Initial project structure
- Spring Boot 3.2.1 application setup
- Vaadin 24.3.3 Flow integration
- Maven build configuration with:
  - Java 21 as target runtime
  - Spring Boot starter dependencies
  - Vaadin Flow starter
  - Spring Boot DevTools for hot reload
- Package structure: `com.sciome.bmdexpressweb`
- Main application class with `@SpringBootApplication`

**2025-10-15 07:00** - Development infrastructure
- Embedded Tomcat server (port 8080)
- Logging configuration with SLF4J and Logback
- Application properties configuration
- Git repository initialization

---

## Version History

### Semantic Versioning
This project uses semantic versioning (MAJOR.MINOR.PATCH):
- **MAJOR**: Incompatible API changes
- **MINOR**: New functionality in a backward-compatible manner
- **PATCH**: Backward-compatible bug fixes

### Release Notes
For detailed release notes and migration guides, see the [GitHub Releases](https://github.com/sciome/BMDExpress-Web/releases) page.

---

## Contributing
When making changes to this project, please update this changelog following these guidelines:

1. Add entries under **[Unreleased]** section with timestamp format: `YYYY-MM-DD HH:MM`
2. Use the following categories:
   - **Added** for new features
   - **Changed** for changes in existing functionality
   - **Deprecated** for soon-to-be removed features
   - **Removed** for now removed features
   - **Fixed** for any bug fixes
   - **Security** for vulnerability fixes
3. Include links to relevant issues or pull requests where applicable
4. Keep entries concise but descriptive
5. List most recent changes first within each category

---

*Last updated: 2025-10-15 17:45*
