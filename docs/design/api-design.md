# API Design

## API Structure

**Base URL:** `https://bmdexpress.sciome.com/api/v1`

**Authentication:** JWT Bearer tokens

## Core Endpoints

### Projects

```
GET    /api/v1/projects              # List user's projects
POST   /api/v1/projects              # Create project
GET    /api/v1/projects/{id}         # Get project details
PUT    /api/v1/projects/{id}         # Update project
DELETE /api/v1/projects/{id}         # Delete project
```

### Data Import

```
POST   /api/v1/projects/{id}/experiments     # Import expression data
GET    /api/v1/experiments/{id}              # Get experiment details
DELETE /api/v1/experiments/{id}              # Delete experiment
```

### Analysis Submission

```
POST   /api/v1/analysis/prefilter/anova      # Submit ANOVA
POST   /api/v1/analysis/prefilter/williams   # Submit Williams trend
POST   /api/v1/analysis/bmd                  # Submit BMD analysis
POST   /api/v1/analysis/category             # Submit category analysis
```

### Job Management

```
GET    /api/v1/jobs                          # List user's jobs
GET    /api/v1/jobs/{jobId}                  # Get job status
DELETE /api/v1/jobs/{jobId}                  # Cancel job
GET    /api/v1/jobs/{jobId}/logs             # Get job logs
```

### Results Retrieval

```
GET    /api/v1/results/bmd/{id}              # Get BMD results
GET    /api/v1/results/category/{id}         # Get category results
GET    /api/v1/results/{id}/export           # Export results
```

## Example API Requests

### Submit BMD Analysis

```http
POST /api/v1/analysis/bmd
Authorization: Bearer {token}
Content-Type: application/json

{
  "projectId": 123,
  "experimentId": 456,
  "prefilterId": 789,
  "name": "BMD Analysis - Hill, Power",
  "method": "BMDS",
  "models": ["HILL", "POWER", "EXPONENTIAL_2", "EXPONENTIAL_3"],
  "inputParameters": {
    "bmr": 1.349,
    "constantVariance": true,
    "restrictPower": true,
    "confidence": 0.95,
    "iterations": 250
  },
  "modelSelection": {
    "flagHillWithKLessThanOneFourth": true,
    "useNestedChiSquared": true,
    "pValueCutoff": 0.05
  }
}
```

**Response:**
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "QUEUED",
  "message": "Analysis job submitted successfully",
  "estimatedDuration": "15 minutes",
  "_links": {
    "self": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "cancel": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }
}
```

### Get Job Status

```http
GET /api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890
Authorization: Bearer {token}
```

**Response:**
```json
{
  "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "jobType": "BMD_ANALYSIS",
  "status": "RUNNING",
  "progress": 0.45,
  "message": "Processing gene 2250 of 5000",
  "createdAt": "2025-10-15T10:00:00Z",
  "startedAt": "2025-10-15T10:00:15Z",
  "estimatedCompletion": "2025-10-15T10:12:00Z",
  "_links": {
    "self": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "cancel": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "logs": "/api/v1/jobs/a1b2c3d4-e5f6-7890-abcd-ef1234567890/logs"
  }
}
```

## WebSocket API

**Endpoint:** `wss://bmdexpress.sciome.com/ws`

**Subscribe to job updates:**
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  stompClient.subscribe('/user/queue/job-progress', function(message) {
    const progress = JSON.parse(message.body);
    updateProgressBar(progress.progress);
    updateStatusMessage(progress.message);
  });
});
```
