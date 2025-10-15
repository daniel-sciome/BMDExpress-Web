# Deployment Architecture

## Production Architecture

```
                          ┌─────────────────┐
                          │   Load Balancer │
                          │     (Nginx)     │
                          └────────┬────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
         ┌──────────▼─────────┐       ┌──────────▼─────────┐
         │  Web Server 1      │       │  Web Server 2      │
         │  (Spring Boot +    │       │  (Spring Boot +    │
         │   Vaadin)          │       │   Vaadin)          │
         └──────────┬─────────┘       └──────────┬─────────┘
                    │                             │
                    └──────────────┬──────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
    ┌─────▼─────┐          ┌──────▼──────┐        ┌───────▼──────┐
    │PostgreSQL │          │    Redis    │        │   MinIO/S3   │
    │ (Primary) │          │(Queue/Cache)│        │(Object Store)│
    └───────────┘          └─────────────┘        └──────────────┘
          │
    ┌─────▼─────┐
    │PostgreSQL │
    │ (Replica) │
    └───────────┘

         ┌──────────────────────────────────────────────┐
         │         Worker Nodes (Kubernetes)            │
         │  ┌─────────────┐  ┌─────────────┐  ┌─────┐  │
         │  │BMDS Worker 1│  │BMDS Worker 2│  │ ... │  │
         │  └─────────────┘  └─────────────┘  └─────┘  │
         │  ┌─────────────┐  ┌─────────────┐  ┌─────┐  │
         │  │ToxicR Wrkr 1│  │ToxicR Wrkr 2│  │ ... │  │
         │  └─────────────┘  └─────────────┘  └─────┘  │
         └──────────────────────────────────────────────┘
```

## Docker Compose (Development)

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: bmdexpress
      POSTGRES_USER: bmduser
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio-data:/data

  bmdexpress-web:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/bmdexpress
      SPRING_DATASOURCE_USERNAME: bmduser
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      MINIO_ENDPOINT: http://minio:9000
    depends_on:
      - postgres
      - redis
      - minio

volumes:
  postgres-data:
  minio-data:
```

## Kubernetes Deployment (Production)

**Key Resources:**

### Web Application Deployment:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bmdexpress-web
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bmdexpress-web
  template:
    metadata:
      labels:
        app: bmdexpress-web
    spec:
      containers:
      - name: bmdexpress-web
        image: sciome/bmdexpress-web:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

### BMDS Worker Deployment:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bmds-worker
spec:
  replicas: 5
  selector:
    matchLabels:
      app: bmds-worker
  template:
    metadata:
      labels:
        app: bmds-worker
    spec:
      containers:
      - name: bmds-worker
        image: sciome/bmds-worker:latest
        env:
        - name: REDIS_HOST
          value: redis-service
        resources:
          requests:
            memory: "4Gi"
            cpu: "2000m"
          limits:
            memory: "8Gi"
            cpu: "4000m"
```

## Monitoring & Observability

**Metrics Collection:**
- **Prometheus** for metrics scraping
- **Micrometer** (Spring Boot Actuator) for metrics exposure
- **Grafana** dashboards for visualization

**Key Metrics:**
- Request rate, latency, error rate
- Job queue depth, job completion rate
- Database connection pool metrics
- JVM memory, garbage collection
- BMDS/ToxicR process metrics

**Logging:**
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- Structured JSON logs
- Correlation IDs for request tracing

**Alerting:**
- **Alertmanager** (Prometheus)
- Slack/email notifications
- Alert rules:
  - High error rate (>5%)
  - Job queue backup (>100 pending)
  - Database connection saturation
  - High memory usage (>90%)
