# Non-Functional Requirements

Bu doküman, Spring AI + Docling entegrasyonu projesinin non-functional (kalite) gereksinimlerini içermektedir.

---

## NFR-001: Performance

### 1.1 Response Time Requirements

| Operation | Target | Maximum |
|-----------|--------|---------|
| Document Conversion (< 10 pages) | < 5 seconds | 15 seconds |
| Document Conversion (10-50 pages) | < 15 seconds | 45 seconds |
| Document Conversion (50+ pages) | < 30 seconds | 90 seconds |
| RAG Query (semantic search) | < 500 ms | 2 seconds |
| RAG Query (full response) | < 3 seconds | 10 seconds |
| Health Check | < 100 ms | 500 ms |

### 1.2 Throughput Requirements

| Metric | Target |
|--------|--------|
| Concurrent document conversions | 10 |
| Concurrent RAG queries | 50 |
| Documents ingested per hour | 100 |

### 1.3 Resource Utilization

| Resource | Target | Maximum |
|----------|--------|---------|
| CPU (application) | < 50% | 80% |
| Memory (heap) | < 512 MB | 1 GB |
| Disk (vector store) | < 10 GB | 50 GB |

---

## NFR-002: Scalability

### 2.1 Horizontal Scaling

| Component | Scalable | Notes |
|-----------|----------|-------|
| Application | Yes | Stateless, can run multiple instances |
| Docling Serve | Yes | Can run multiple containers |
| OpenAI API | Yes | Cloud-based, auto-scaling |
| PGVector | Yes | Read replicas supported |

### 2.2 Vertical Scaling

| Component | Recommended | Notes |
|-----------|-------------|-------|
| Application | 2 CPU, 1 GB RAM | Increase for heavy load |
| Docling Serve | 4 CPU, 4 GB RAM | CPU-intensive for OCR |
| PostgreSQL | 2 CPU, 2 GB RAM | Increase for large vector store |

### 2.3 Data Volume

| Metric | Initial | Target (1 year) |
|--------|---------|-----------------|
| Documents | 100 | 10,000 |
| Chunks | 1,000 | 100,000 |
| Embeddings | 1,000 | 100,000 |
| Vector Store Size | 100 MB | 10 GB |

---

## NFR-003: Security

### 3.1 Data Protection

| Requirement | Implementation |
|-------------|----------------|
| Data at rest encryption | PostgreSQL encryption, OS-level encryption |
| Data in transit encryption | TLS 1.3 for all HTTP communication |
| No external data transmission | All processing on-premises |
| Secrets management | Environment variables, Kubernetes secrets |

### 3.2 Access Control

| Requirement | Implementation |
|-------------|----------------|
| API authentication | OAuth 2.0 / JWT (production) |
| API authorization | Role-based access control |
| Actuator endpoints | Secured in production |
| Admin access | IP whitelisting, VPN |

### 3.3 Audit & Compliance

| Requirement | Implementation |
|-------------|----------------|
| Request logging | Structured logging with correlation ID |
| Audit trail | Database audit log for document operations |
| Data retention | Configurable retention policy |
| GDPR compliance | Data deletion capability |

### 3.4 Vulnerability Management

| Requirement | Implementation |
|-------------|----------------|
| Dependency scanning | Dependabot, Snyk |
| Container scanning | Trivy, Clair |
| SAST | SonarQube |
| Penetration testing | Annual third-party assessment |

---

## NFR-004: Observability

### 4.1 Logging

| Level | Use Case |
|-------|----------|
| ERROR | System failures, exceptions |
| WARN | Degraded performance, retries |
| INFO | Request/response summary, key events |
| DEBUG | Detailed flow, variable values (dev only) |

**Log Format:**
```json
{
  "timestamp": "2025-12-23T10:00:00Z",
  "level": "INFO",
  "correlationId": "uuid",
  "service": "docling-sample",
  "message": "Document converted",
  "documentId": "uuid",
  "duration": 1234
}
```

### 4.2 Metrics

| Category | Metrics |
|----------|---------|
| **Application** | `http.server.requests`, `jvm.memory.used`, `jvm.threads.live` |
| **Document Processing** | `docling.conversion.count`, `docling.conversion.duration`, `docling.conversion.errors` |
| **RAG** | `rag.query.count`, `rag.query.duration`, `rag.retrieval.count` |
| **Infrastructure** | `docling.health.status`, `openai.health.status`, `db.connection.pool` |

### 4.3 Tracing

| Requirement | Implementation |
|-------------|----------------|
| Distributed tracing | OpenTelemetry |
| Trace propagation | W3C Trace Context |
| Span collection | Document conversion, embedding, search, generation |
| Visualization | Grafana, Jaeger |

### 4.4 Alerting

| Alert | Condition | Severity |
|-------|-----------|----------|
| `HighErrorRate` | Error rate > 5% for 5 min | Critical |
| `SlowResponseTime` | P95 latency > 10s for 5 min | Warning |
| `ServiceDown` | Health check failed for 1 min | Critical |
| `HighMemoryUsage` | Memory > 80% for 10 min | Warning |
| `DiskSpaceLow` | Disk < 20% free | Warning |

---

## NFR-005: Maintainability

### 5.1 Code Quality

| Metric | Target |
|--------|--------|
| Test coverage | > 80% |
| Code duplication | < 3% |
| Cyclomatic complexity | < 10 per method |
| Technical debt ratio | < 5% |

### 5.2 Documentation

| Type | Requirement |
|------|-------------|
| API documentation | OpenAPI 3.0 spec |
| Code documentation | Javadoc for public APIs |
| Architecture documentation | C4 diagrams, ADRs |
| Runbook | Operational procedures |

### 5.3 Deployment

| Requirement | Implementation |
|-------------|----------------|
| CI/CD | GitHub Actions / GitLab CI |
| Automated testing | Unit, integration, e2e |
| Blue-green deployment | Zero-downtime deployments |
| Rollback capability | < 5 minutes |

### 5.4 Dependency Management

| Requirement | Implementation |
|-------------|----------------|
| Version pinning | BOM (Bill of Materials) |
| Vulnerability updates | Monthly review, critical within 48h |
| Compatibility testing | Integration tests with new versions |

---

## NFR-006: Reliability

### 6.1 Availability

| Environment | Target | Notes |
|-------------|--------|-------|
| Development | Best effort | Dev Services may restart |
| Staging | 95% | Business hours |
| Production | 99.5% | 24/7 |

### 6.2 Fault Tolerance

| Failure Scenario | Behavior |
|------------------|----------|
| Docling Serve unavailable | Return 503, health check DOWN |
| OpenAI API unavailable | Return 503, RAG queries fail gracefully |
| Database unavailable | Return 503, all operations fail |
| Network partition | Graceful degradation, retry with backoff |

### 6.3 Recovery

| Metric | Target |
|--------|--------|
| RTO (Recovery Time Objective) | < 15 minutes |
| RPO (Recovery Point Objective) | < 1 hour |
| MTTR (Mean Time To Recovery) | < 30 minutes |

### 6.4 Backup & Restore

| Data | Frequency | Retention |
|------|-----------|-----------|
| Vector store | Daily | 30 days |
| Configuration | On change | 90 days |
| Logs | Continuous | 30 days |

---

## NFR-007: Usability (Developer Experience)

### 7.1 Development Setup

| Requirement | Target |
|-------------|--------|
| Time to first run | < 10 minutes |
| Prerequisites | JDK 25, Docker/Podman |
| Documentation clarity | Beginner-friendly README |

### 7.2 API Design

| Requirement | Implementation |
|-------------|----------------|
| Consistent naming | RESTful conventions |
| Error messages | Descriptive, actionable |
| API versioning | URL path versioning |
| Rate limiting | Configurable, clear headers |

### 7.3 Debugging

| Requirement | Implementation |
|-------------|----------------|
| Local debugging | IDE support, hot reload |
| Log correlation | Request ID propagation |
| Error tracing | Stack traces in development |

---

## NFR Summary Matrix

| ID | Category | Priority | Status |
|----|----------|----------|--------|
| NFR-001 | Performance | High | Defined |
| NFR-002 | Scalability | Medium | Defined |
| NFR-003 | Security | High | Defined |
| NFR-004 | Observability | High | Defined |
| NFR-005 | Maintainability | Medium | Defined |
| NFR-006 | Reliability | High | Defined |
| NFR-007 | Usability | Medium | Defined |

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-23 | AI Assistant | Initial NFR documentation |
| 1.1 | 2025-12-24 | AI Assistant | Switched from Ollama to OpenAI GPT-4o |