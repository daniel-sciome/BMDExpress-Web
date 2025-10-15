# Desktop Application Analysis

## Architecture Overview

**BMDExpress-3** is a JavaFX desktop application built on Maven with Java 21.

**Location:** `/home/svobodadl/BMDExpress-3/`
**Package:** `com.sciome.bmdexpress2`
**Main Class:** `BMDExpress3Main.java`
**Architecture Pattern:** Model-View-Presenter (MVP)
**Codebase Size:** 550 Java files across 98 packages

## Architectural Pattern: MVP

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

## Domain Model Structure

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

## Service Layer

| Service | Responsibility | Key Dependencies |
|---------|---------------|------------------|
| `BMDAnalysisService` | BMD curve fitting | BMDS, ToxicR, GCurveP |
| `PrefilterService` | Statistical prefiltering | Apache Commons Math, Sciome Math |
| `CategoryAnalysisService` | Pathway enrichment | Fisher's exact test, IVIVE |
| `ProjectNavigationService` | File I/O, serialization | Jackson, Java serialization |
| `BMDStatisticsService` | Statistical metrics | Apache Commons Math |

**Key Observation:** Services are well-abstracted with interfaces and minimal UI coupling. These can be reused directly in Spring.

## UI Layer

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

## Command-Line Interface

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

## Key Dependencies

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
