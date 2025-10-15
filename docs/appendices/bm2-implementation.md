# Appendix A: .bm2 File Implementation

## Overview

This appendix documents the implementation of `.bm2` file support in BMDExpress-Web. This represents a **deviation from the original design** documented in the main design documents, which focused on a database-first approach.

## Design Deviation

### Original Plan
The original implementation plan (see [Implementation Phases](../design/implementation-phases.md)) outlined a database-first approach using:

- PostgreSQL for persistent storage
- JPA entities for BMDProject, BMDData, BMDAnalysis
- Flyway migrations for schema management
- REST API for CRUD operations on database-backed entities

### Adopted Approach
Instead of starting with database persistence, we implemented a **parallel .bm2 file-based approach** that:

- Uses the desktop application's native binary serialization format (`.bm2` files)
- Stores deserialized projects in-memory using `ConcurrentHashMap`
- Provides REST API for uploading and managing `.bm2` files
- Reuses the desktop application JAR as a Maven dependency

### Rationale
This approach was chosen to:

1. **Maintain compatibility** with the existing BMDExpress desktop application
2. **Leverage existing code** developed prior to this project
3. **Accelerate development** by reusing proven serialization mechanisms
4. **Enable gradual migration** - users can continue using `.bm2` files while we develop database persistence
5. **Simplify testing** - existing `.bm2` project files can be used directly

## BMDExpress Desktop Application JAR Dependency

### Dependency Details

The BMDExpress-Web application uses the BMDExpress3 desktop application JAR as a Maven dependency:

```xml
<dependency>
    <groupId>com.sciome</groupId>
    <artifactId>bmdexpress3</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

**JAR Location:** `/home/svobodadl/.m2/repository/com/sciome/bmdexpress3/3.0.0-SNAPSHOT/`

### Installation Process

The JAR was installed to the local Maven repository using:

```bash
mvn install:install-file \
  -Dfile=/tmp/bmdexpress3-3.0.0-SNAPSHOT.jar \
  -DgroupId=com.sciome \
  -DartifactId=bmdexpress3 \
  -Dversion=3.0.0-SNAPSHOT \
  -Dpackaging=jar
```

### Classes Used

The web application imports the following classes from the desktop JAR:

- **`com.sciome.bmdexpress2.mvp.model.BMDProject`** - Root project model containing all analysis data
- **`com.sciome.bmdexpress2.mvp.model.stat.BMDResult`** - BMD analysis results
- **`com.sciome.bmdexpress2.shared.BMDExpressProperties`** - Shared application properties

These classes are serializable and support Java's `ObjectInputStream` deserialization mechanism.

## Leveraging Existing Code

### Source Code Origin

Prior to this BMDExpress-Web project, prototype server code was developed and placed in `/tmp/server/`. This code demonstrated:

- REST API endpoints for project management
- `.bm2` file deserialization using `ObjectInputStream`
- In-memory project storage with metadata tracking
- Category analysis endpoints (for future integration)

### Code Adaptation Strategy

Rather than starting from scratch, we **adapted** this existing code to fit our project structure:

| Original Package | Adapted Package |
|-----------------|-----------------|
| `com.sciome.bmdexpress2.server.service` | `com.sciome.bmdexpressweb.service` |
| `com.sciome.bmdexpress2.server.controller` | `com.sciome.bmdexpressweb.controller` |
| `com.sciome.bmdexpress2.server.dto` | `com.sciome.bmdexpressweb.dto` |

**Key Changes:**
- Updated package names to match our architecture (see [Project Structure](../design/project-structure.md))
- Modified file paths to use `data/projects/` directory instead of hardcoded paths
- Added comprehensive logging and documentation
- Integrated with our Spring Boot configuration

## Implementation Details

### Project Management Service

**File:** `src/main/java/com/sciome/bmdexpressweb/service/ProjectManagementService.java`

This service provides:

- **`loadProject(InputStream, String)`** - Deserializes `.bm2` files using `ObjectInputStream`
- **`getProject(String)`** - Retrieves loaded project by UUID
- **`findBmdResult(String, String)`** - Finds BMDResult by name within a project
- **`getBmdResultNames(String)`** - Lists all BMDResult names in a project
- **`deleteProject(String)`** - Removes project from memory

**Storage:** In-memory `ConcurrentHashMap<String, ProjectHolder>` mapping UUID → project+metadata

**Metadata Tracking:** Each project is wrapped in a `ProjectHolder` that stores:
- Project ID (UUID)
- Original filename
- Upload timestamp
- The `BMDProject` object itself

### .bm2 File Format

The `.bm2` format is BMDExpress's proprietary binary serialization format created using Java's standard serialization mechanism:

```java
// Deserialization (simplified)
ObjectInputStream ois = new ObjectInputStream(inputStream);
BMDProject project = (BMDProject) ois.readObject();
```

**Advantages:**
- Native Java serialization - no custom parsing required
- Preserves complete object graphs including nested analysis results
- Backward compatible with desktop application files

**Limitations:**
- Binary format - not human-readable
- Tied to Java serialization versioning
- Cannot be easily queried without full deserialization

### Data Storage

**Directory:** `data/projects/`

This directory is used for:
- Storing user-uploaded `.bm2` files (optional - projects can be loaded directly into memory)
- Providing a location for server-side `.bm2` files that can be loaded via API
- Testing with sample project files

**Git Ignore:** The `data/` directory is excluded from version control (see `.gitignore`)

## REST API Endpoints

The following endpoints are implemented (or planned) in `ProjectController`:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/projects` | POST | Upload `.bm2` file and load into memory |
| `/api/projects/{id}` | GET | Get project metadata |
| `/api/projects/{id}/full` | GET | Get complete BMDProject object |
| `/api/projects/{id}/bmd-results` | GET | List BMDResult names |
| `/api/projects/{id}` | DELETE | Remove project from memory |
| `/api/projects/available-files` | GET | List server-side `.bm2` files |
| `/api/projects/load-from-file` | POST | Load server-side `.bm2` file |

See [API Design](../design/api-design.md) for complete API specification.

## Initialization Requirements

The desktop application requires initialization of its properties system. This is done in `Application.java`:

```java
public static void main(String[] args) {
    // Initialize BMDExpress properties for console/server mode
    com.sciome.bmdexpress2.shared.BMDExpressProperties.getInstance()
        .setIsConsole(true);

    SpringApplication.run(Application.class, args);
}
```

This ensures the desktop library operates in headless mode without requiring JavaFX or GUI components.

## Future Considerations

### Database Integration

The `.bm2` file approach is **not mutually exclusive** with database persistence. Future phases may implement:

1. **Hybrid Approach:**
   - Upload `.bm2` file → deserialize → persist to database
   - Store both file and structured database representation
   - Query database for analysis results
   - Export back to `.bm2` format for desktop compatibility

2. **Migration Path:**
   - Current: In-memory storage (session-scoped)
   - Phase 2: Add database persistence while maintaining `.bm2` import/export
   - Phase 3: Make database the primary storage, `.bm2` as interchange format

### Scalability Considerations

**Current Limitations:**
- Projects stored in-memory - lost on server restart
- No persistence layer
- Memory usage grows with number of loaded projects
- Single-server architecture only

**Future Improvements:**
- Database persistence for durability
- Redis caching layer for frequently accessed projects
- MinIO object storage for `.bm2` files
- Distributed caching for multi-server deployments

See [Implementation Phases](../design/implementation-phases.md) for the full roadmap.

## Code Structure

```
src/main/java/com/sciome/bmdexpressweb/
├── service/
│   └── ProjectManagementService.java    # .bm2 loading and project management
├── controller/
│   └── ProjectController.java           # REST API for project operations
├── dto/
│   ├── ProjectUploadResponse.java       # Response DTO with project metadata
│   └── ErrorResponse.java               # Standard error response
└── Application.java                     # Entry point with BMDExpress init
```

## Testing

Sample `.bm2` files can be obtained from:
- BMDExpress desktop application (File → Save Project)
- Existing project files from `/home/svobodadl/BMDExpress-3/projects/`
- Test data repository (to be created)

## References

- [BMDExpress Desktop Application](https://github.com/auerbachs/BMDExpress-3)
- [Implementation Phases](../design/implementation-phases.md) - Original database-first plan
- [Architecture](../design/architecture.md) - Overall system architecture
- [Migration Strategy](../design/migration-strategy.md) - Desktop to web migration approach

---

**Last Updated:** 2025-10-15
