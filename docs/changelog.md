# Changelog

All notable changes to the BMDExpress-Web project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

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

*Last updated: 2025-10-15 09:30*
