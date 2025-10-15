# Background

## BMDExpress Project History

BMDExpress is a family of applications for benchmark dose (BMD) analysis of genomic data:

- **BMDExpress 1.0** (2007): Original desktop application by Longlong Yang
- **BMDExpress 2.0** (2018): Updated through NTP/Sciome/Health Canada/EPA collaboration
- **BMDExpress 3.0** (2022): Current JavaFX application with ToxicR integration

## Scientific Context

**Benchmark Dose Modeling** estimates the dose at which a biological response deviates from control levels by a predetermined benchmark response (BMR). For genomic data, BMDExpress:

1. Identifies dose-responsive genes through statistical prefiltering
2. Fits dose-response curves using multiple mathematical models
3. Calculates BMD, BMDL (lower confidence limit), and BMDU (upper confidence limit)
4. Performs pathway enrichment analysis to identify affected biological processes
5. Optionally applies IVIVE (In Vitro to In Vivo Extrapolation) for translational toxicology

## Current Limitations of Desktop Application

- **Single-user**: No collaboration features
- **Local compute**: Limited by desktop hardware
- **Installation complexity**: Requires Java, R, and native libraries
- **No centralized data**: Projects stored locally
- **Manual updates**: Users must download new versions
