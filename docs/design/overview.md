# Executive Summary

BMDExpress-Web is a Spring Boot/Vaadin web application that ports the BMDExpress-3 desktop application to a multi-user, cloud-ready platform. The application enables researchers to analyze high-dimensional dose-response data (particularly gene expression data) using EPA BMDS software and ToxicR models to calculate benchmark dose (BMD) values.

## Key Objectives

- **Multi-user access**: Enable collaborative research across institutions
- **Cloud deployment**: Scalable infrastructure for computationally intensive analyses
- **Modern web UI**: Accessible from any browser without desktop installation
- **API-first design**: Support programmatic access and integrations
- **Maintain scientific integrity**: Preserve validated algorithms from desktop version

## Success Criteria

- 100% feature parity with BMDExpress-3 for core analysis workflows
- Support for concurrent users with isolated workspaces
- Sub-5-second response times for interactive operations
- Successful migration of existing .bm2 project files
- RESTful API for headless batch processing

## Background Overview

### BMDExpress Project History

BMDExpress is a family of applications for benchmark dose (BMD) analysis of genomic data:

- **BMDExpress 1.0** (2007): Original desktop application by Longlong Yang
- **BMDExpress 2.0** (2018): Updated through NTP/Sciome/Health Canada/EPA collaboration
- **BMDExpress 3.0** (2022): Current JavaFX application with ToxicR integration

### Scientific Context

**Benchmark Dose Modeling** estimates the dose at which a biological response deviates from control levels by a predetermined benchmark response (BMR). For genomic data, BMDExpress:

1. Identifies dose-responsive genes through statistical prefiltering
2. Fits dose-response curves using multiple mathematical models
3. Calculates BMD, BMDL (lower confidence limit), and BMDU (upper confidence limit)
4. Performs pathway enrichment analysis to identify affected biological processes
5. Optionally applies IVIVE (In Vitro to In Vivo Extrapolation) for translational toxicology
