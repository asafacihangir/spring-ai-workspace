# Use Case: UC-004 - Health Monitoring

**System:** Spring AI + Docling Document Processing System

**Primary Actor:** Operations Team / Monitoring System

**Goal:** Sistemin ve bağımlılıklarının sağlık durumunu izlemek

**Related Requirements:** FR-016

---

## Preconditions

1. Spring Boot Actuator konfigüre edilmiş
2. Health endpoint'i erişilebilir durumda
3. Monitoring sistemi (Grafana, Prometheus vb.) entegre (opsiyonel)

## Postconditions

1. Sistem durumu raporlanmış
2. Bağımlılık durumları görüntülenmiş
3. Alerting mekanizması tetiklenmiş (eğer down ise)

---

## Main Success Scenario

1. Actor, `/actuator/health` endpoint'ine request gönderir.
2. Sistem, tüm health indicator'ları toplar.
3. Sistem, Docling Serve health check yapar.
4. Sistem, database connection check yapar.
5. Sistem, Ollama service check yapar.
6. Sistem, tüm component durumlarını aggregate eder.
7. Sistem, JSON formatında health report döner.
8. Actor, sistem durumunu değerlendirir.

---

## Extensions

### 3a. Docling Serve erişilemez:
- **3a1.** Health indicator, "docling: DOWN" döner.
- **3a2.** Overall status, "DOWN" olarak belirlenir.
- **3a3.** Alerting sistemi tetiklenir.

### 4a. Database bağlantı hatası:
- **4a1.** Health indicator, "db: DOWN" döner.
- **4a2.** Overall status, "DOWN" olarak belirlenir.

### 5a. Ollama erişilemez:
- **5a1.** Health indicator, "ollama: DOWN" döner.
- **5a2.** RAG functionality unavailable.

### 6a. Partial failure:
- **6a1.** Bazı component'ler UP, bazıları DOWN.
- **6a2.** Overall status, en kritik component'e göre belirlenir.
- **6a3.** Detailed breakdown döner.

---

## Variations

### 1. Health Check Depth:
- a) Basic (UP/DOWN only)
- b) Detailed (component breakdown)
- c) Full (metrics included)

### 2. Access Control:
- a) Public (development)
- b) Authenticated (production)
- c) Internal only (k8s probes)

### 3. Response Format:
- a) JSON (default)
- b) Prometheus metrics
- c) Plain text

---

## Technical Notes

### Feature Location
- **Feature:** `health`
- **Use Case:** `health/application/CheckHealthUseCase.java`
- **Controller:** `health/api/HealthController.java`
- **Indicators:** `health/infrastructure/DoclingHealthIndicator.java`, `OpenAIHealthIndicator.java`, `VectorStoreHealthIndicator.java`

### Configuration
```yaml
management:
  endpoint:
    health:
      show-components: always
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### API Endpoint
```
GET /actuator/health
```

### Example Response
```json
{
  "status": "UP",
  "components": {
    "docling": {
      "status": "UP",
      "details": {
        "url": "http://localhost:8001"
      }
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "ollama": {
      "status": "UP",
      "details": {
        "models": ["granite4:3b", "granite-embedding:278m"]
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 123456789012
      }
    }
  }
}
```

---

## Kubernetes Integration

### Liveness Probe
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

### Readiness Probe
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## Monitoring Dashboard

| Panel | Description |
|-------|-------------|
| **System Status** | Overall UP/DOWN indicator |
| **Component Health** | Individual component status |
| **Response Time** | Health check latency |
| **Uptime** | Historical availability |
| **Alerts** | Active/resolved alerts |

---

## Alert Rules

| Alert | Condition | Severity |
|-------|-----------|----------|
| `DoclingDown` | docling.status == DOWN for 1m | Critical |
| `DatabaseDown` | db.status == DOWN for 30s | Critical |
| `OllamaDown` | ollama.status == DOWN for 2m | Warning |
| `HighLatency` | health.duration > 5s | Warning |