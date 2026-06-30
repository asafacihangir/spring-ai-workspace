# Test Scenarios - Spring AI + Docling RAG System

Bu döküman, sistemin uçtan uca test edilmesi için adım adım senaryoları içerir.

---

## Önkoşullar

### 1. Servisleri Başlat

```bash
# Docker servislerini başlat (PostgreSQL + PGVector, Docling)
docker compose up -d

# Servislerin hazır olduğunu kontrol et
docker ps
```

### 2. Uygulamayı Başlat

```bash
# Java 25 ile uygulamayı çalıştır
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 25.0.1-tem && mvn spring-boot:run
```

### 3. Health Check

```bash
# Tüm servislerin sağlıklı olduğunu doğrula
curl -s http://localhost:8080/actuator/health | jq
```

**Beklenen Sonuç:**
```json
{
  "status": "UP",
  "components": {
    "docling": { "status": "UP" },
    "db": { "status": "UP" },
    "openai": { "status": "UP" }
  }
}
```

---

## Senaryo 1: Basit PDF Ingestion ve Chat

### Adım 1: PDF Dokümanı Ingest Et

Küçük bir PDF dosyasını vector store'a yükle:

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{"url": "https://arxiv.org/pdf/2408.09869"}'
```

**Beklenen Sonuç:**
```json
{
  "success": true,
  "documentCount": 15,
  "message": "Successfully ingested 15 document chunks"
}
```

> **Not:** İlk istek Docling'in PDF'i parse etmesi nedeniyle 1-5 dakika sürebilir.

### Adım 2: Ingested Doküman Hakkında Soru Sor

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is Docling and what are its main features?"}'
```

**Beklenen Sonuç:**
```json
{
  "answer": "Docling is a document processing library that...",
  "sources": ["chunk-1", "chunk-5", "chunk-8"]
}
```

---

## Senaryo 2: Dosya Yolu ile Ingestion

### Adım 1: Lokal PDF Dosyasını Ingest Et

```bash
# Önce bir PDF dosyası indir
curl -o /tmp/sample.pdf https://www.w3.org/WAI/WCAG21/Techniques/pdf/img/table-word.pdf

# Dosyayı ingest et
curl -X GET "http://localhost:8080/api/documents/ingest/file?path=/tmp/sample.pdf"
```

**Beklenen Sonuç:**
```json
{
  "success": true,
  "documentCount": 3,
  "message": "Successfully ingested 3 document chunks"
}
```

### Adım 2: Doküman İçeriği Hakkında Soru Sor

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What does the document say about tables?"}'
```

---

## Senaryo 3: Document Conversion (Ingestion Olmadan)

Bir dokümanı sadece markdown'a çevir (vector store'a kaydetme):

### Adım 1: URL'den Dönüştür

```bash
curl -X GET "http://localhost:8080/api/documents/convert/http?url=https://arxiv.org/pdf/2408.09869" | jq
```

**Beklenen Sonuç:**
```json
{
  "success": true,
  "markdownContent": "# Document Title\n\n## Abstract\n\nThis paper presents...",
  "errors": []
}
```

### Adım 2: POST ile Dönüştür

```bash
curl -X POST http://localhost:8080/api/documents/convert \
  -H "Content-Type: application/json" \
  -d '{"url": "https://arxiv.org/pdf/2408.09869"}' | jq
```

---

## Senaryo 4: Çoklu Doküman Ingestion

### Adım 1: İlk Dokümanı Ingest Et

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{"url": "https://arxiv.org/pdf/2408.09869"}'
```

### Adım 2: İkinci Dokümanı Ingest Et

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.w3.org/WAI/WCAG21/Techniques/pdf/img/table-word.pdf"}'
```

### Adım 3: Her İki Dokümandan Bilgi İste

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Compare the topics covered in the documents I uploaded"}'
```

---

## Senaryo 5: Hata Durumları

### 5.1 Geçersiz URL

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{"url": "https://invalid-url-that-does-not-exist.com/doc.pdf"}'
```

**Beklenen Sonuç:**
```json
{
  "success": false,
  "documentCount": 0,
  "message": "No documents were parsed from source"
}
```

### 5.2 Eksik Parametre

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Beklenen Sonuç:**
```json
{
  "success": false,
  "documentCount": 0,
  "message": "Either url or filePath must be provided"
}
```

### 5.3 Boş URL

```bash
curl -X GET "http://localhost:8080/api/documents/ingest/http?url="
```

**Beklenen Sonuç:**
```json
{
  "success": false,
  "documentCount": 0,
  "message": "URL is required"
}
```

---

## Senaryo 6: RAG Chat Akışı (End-to-End)

Bu senaryo, tam bir RAG (Retrieval Augmented Generation) akışını test eder.

### Adım 1: Vector Store'u Temizle (Opsiyonel)

```sql
-- PostgreSQL'e bağlan ve tabloyu temizle
TRUNCATE TABLE vector_store;
```

### Adım 2: Teknik Doküman Ingest Et

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -H "Content-Type: application/json" \
  -d '{"url": "https://arxiv.org/pdf/2408.09869"}'

echo "Docling paper ingested"
```

### Adım 3: Genel Soru Sor

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the main contribution of this paper?"}'
```

### Adım 4: Detaylı Soru Sor

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "How does Docling handle PDF tables?"}'
```

### Adım 5: Doküman Dışı Soru Sor

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the capital of France?"}'
```

**Beklenen Davranış:** Sistem, vector store'da ilgili bilgi bulamadığını belirtmeli veya genel bilgisiyle yanıt vermeli.

---

## Test Kontrol Listesi

| # | Test | Endpoint | Beklenen Sonuç |
|---|------|----------|----------------|
| 1 | Health check | `GET /actuator/health` | status: UP |
| 2 | URL ile ingest | `POST /api/documents/ingest` | success: true |
| 3 | File ile ingest | `GET /api/documents/ingest/file` | success: true |
| 4 | URL ile convert | `GET /api/documents/convert/http` | success: true |
| 5 | POST ile convert | `POST /api/documents/convert` | success: true |
| 6 | Chat query | `POST /api/chat` | answer döner |
| 7 | Geçersiz URL | `POST /api/documents/ingest` | success: false |
| 8 | Eksik parametre | `POST /api/documents/ingest` | error message |

---

## Performans Notları

| İşlem | Beklenen Süre |
|-------|---------------|
| Küçük PDF ingestion (< 10 sayfa) | 30s - 2 dakika |
| Büyük PDF ingestion (> 50 sayfa) | 2 - 10 dakika |
| Chat query | 1 - 5 saniye |
| Document conversion | 30s - 5 dakika |

> **Timeout:** Sistem 10 dakikalık read timeout ile yapılandırılmıştır. Çok büyük dokümanlar için bu süre yeterli olmalıdır.

---

## Troubleshooting

### Docling Timeout Hatası

```
Request timed out
```

**Çözüm:** Docling container'ının çalıştığını kontrol et:
```bash
docker logs docling-serve
```

### OpenAI API Hatası

```
401 Unauthorized
```

**Çözüm:** `OPENAI_API_KEY` environment variable'ını kontrol et:
```bash
echo $OPENAI_API_KEY
```

### PostgreSQL Bağlantı Hatası

```
Connection refused
```

**Çözüm:** PostgreSQL container'ının çalıştığını kontrol et:
```bash
docker ps | grep postgres
```
