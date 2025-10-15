# BMDExpress-Web Documentation

Welcome to the **BMDExpress-Web** project documentation.

## Overview

BMDExpress-Web is a Spring Boot/Vaadin web application that ports the BMDExpress-3 desktop application to a multi-user, cloud-ready platform. The application enables researchers to analyze high-dimensional dose-response data (particularly gene expression data) using EPA BMDS software and ToxicR models to calculate benchmark dose (BMD) values.

## Key Features

- **Multi-user access**: Enable collaborative research across institutions
- **Cloud deployment**: Scalable infrastructure for computationally intensive analyses
- **Modern web UI**: Accessible from any browser without desktop installation
- **API-first design**: Support programmatic access and integrations
- **Maintain scientific integrity**: Preserve validated algorithms from desktop version

## Quick Links

- [Design Document Overview](design/overview.md) - Executive summary and project goals
- [Architecture](design/architecture.md) - System architecture and design patterns
- [Implementation Roadmap](design/implementation-phases.md) - 20-week development plan
- [API Design](design/api-design.md) - REST API specifications

## Project Information

- **Package**: `com.sciome.bmdexpressweb`
- **Port from**: BMDExpress-3 JavaFX desktop application
- **Target**: Multi-user web application for dose-response analysis
- **Technology Stack**: Spring Boot 3.2.1 + Vaadin 24.3.3

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for local development)
- PostgreSQL 15+
- Redis 7+

### Build and Run

```bash
# Clone the repository
git clone https://github.com/sciome/BMDExpress-Web.git
cd BMDExpress-Web

# Start infrastructure
docker-compose up -d

# Build and run
mvn spring-boot:run
```

Access the application at [http://localhost:8080](http://localhost:8080)

## Documentation Sections

### Design Documentation

Comprehensive design and architecture documentation for the web application:

- **[Overview](design/overview.md)** - Executive summary and objectives
- **[Background](design/background.md)** - Project history and scientific context
- **[Desktop Analysis](design/desktop-analysis.md)** - Analysis of BMDExpress-3 architecture
- **[Web Architecture](design/architecture.md)** - Proposed web application architecture
- **[Migration Strategy](design/migration-strategy.md)** - Approach to porting the codebase
- **[Technology Stack](design/technology-stack.md)** - Technologies and libraries
- **[Project Structure](design/project-structure.md)** - Code organization
- **[Implementation Phases](design/implementation-phases.md)** - Development roadmap
- **[Key Components](design/key-components.md)** - Domain models, services, and UI
- **[Data Model](design/data-model.md)** - Database schema and storage strategy
- **[API Design](design/api-design.md)** - REST API specifications
- **[Security](design/security.md)** - Authentication and authorization
- **[Deployment](design/deployment.md)** - Infrastructure and deployment
- **[Testing](design/testing.md)** - Testing strategy and tools
- **[Risk Mitigation](design/risk-mitigation.md)** - Risks and contingency plans
- **[Glossary](design/glossary.md)** - Terms and definitions

## Contributing

This project is developed by Sciome LLC in collaboration with NIEHS.

### Team

- **Dan Svoboda** - Lead Designer
- **Jason Phillips** - Software Engineer
- **Scott Auerbach** - Project Lead (NIEHS)

## License

BMDExpress Copyright © 2015-2025 by National Institute of Environmental Health Sciences. All rights reserved.
