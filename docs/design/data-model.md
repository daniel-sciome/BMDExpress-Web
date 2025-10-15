# Data Model

## Database Schema Overview

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

## Core Tables

### users
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

### bmd_projects
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

### dose_response_experiments
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

### bmd_results
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

### jobs
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

## Storage Strategy

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
