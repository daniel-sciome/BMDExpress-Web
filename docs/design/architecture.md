# Web Application Architecture

## Overall Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser)                         │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │          Vaadin UI (Server-side rendering)              │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐   │  │
│  │  │ Project │  │  Data   │  │Analysis │  │ Charts  │   │  │
│  │  │  Views  │  │ Import  │  │  Views  │  │  Views  │   │  │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘   │  │
│  └─────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              │ WebSocket (real-time updates)   │
│                              ▼                                  │
└─────────────────────────────────────────────────────────────────┘
                               │
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                      │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    Vaadin Views                          │  │
│  └─────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                 REST Controllers (API)                   │  │
│  └─────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                   Service Layer                          │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
│  │  │   Analysis  │  │   Project   │  │     Job     │     │  │
│  │  │   Services  │  │   Services  │  │   Services  │     │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
│  └─────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                   Repository Layer                       │  │
│  │               (Spring Data JPA)                          │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                    │                  │                │
         ───────────┴──────────────────┴────────────────┴──────
                    ▼                  ▼                ▼
         ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐
         │   PostgreSQL     │  │    Redis     │  │  MinIO/S3    │
         │   (Metadata)     │  │ (Queue/Cache)│  │ (File Store) │
         └──────────────────┘  └──────────────┘  └──────────────┘

         ┌─────────────────────────────────────────────────────┐
         │              Worker Nodes (Async)                   │
         │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
         │  │ BMDS Docker  │  │ToxicR Docker │  │  Job     │  │
         │  │  Container   │  │  Container   │  │ Executor │  │
         │  └──────────────┘  └──────────────┘  └──────────┘  │
         └─────────────────────────────────────────────────────┘
```

## Architectural Principles

1. **Separation of Concerns**: Clear boundaries between layers
2. **API-First**: REST endpoints for all operations
3. **Event-Driven**: Spring Events + WebSocket for real-time updates
4. **Async by Default**: Long-running analyses in background jobs
5. **Stateless Services**: Business logic independent of session state
6. **Multi-Tenancy**: User isolation at database and application levels

## Key Architectural Changes from Desktop

| Aspect | Desktop | Web |
|--------|---------|-----|
| **State Management** | Single `BMDProject` in memory | Database-backed per-user projects |
| **Communication** | EventBus (in-process) | Spring Events + WebSocket |
| **UI Framework** | JavaFX | Vaadin |
| **Persistence** | Java serialization to .bm2 | PostgreSQL + JPA |
| **Processing** | Synchronous, single-threaded UI | Async jobs with progress tracking |
| **Scalability** | Single machine | Horizontal scaling with load balancer |
| **Authentication** | None | Spring Security with user accounts |
