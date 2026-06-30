# Use Case: UC-002 - Document Ingestion

**System:** Spring AI + Docling RAG System

**Primary Actor:** System (Automated) / Developer

**Goal:** Dokümanları parse edip, chunk'lara ayırıp, embedding'lere dönüştürüp vector store'a kaydetmek

**Related Requirements:** FR-003, FR-009, FR-010, FR-011

---

## Preconditions

1. Docling Serve servisi çalışır durumda
2. Vector Store (PGVector) erişilebilir durumda
3. OpenAI API erişilebilir (text-embedding-3-small için)
4. İşlenecek dokümanlar mevcut

## Postconditions

1. Dokümanlar başarıyla parse edilmiş
2. Hierarchical chunking uygulanmış
3. Her chunk için embedding oluşturulmuş
4. Embedding'ler vector store'a kaydedilmiş
5. Semantic search için hazır durumda

---

## Main Success Scenario

1. Sistem başlatılır (application startup).
2. IngestionPipeline component'i `@PostConstruct` ile tetiklenir.
3. Sistem, classpath'teki dokümanları yükler.
4. DoclingDocumentReader, dokümanları Docling Serve'e gönderir.
5. Docling Serve, dokümanları parse eder ve hierarchical chunking uygular.
6. Sistem, Spring AI Document listesi alır.
7. Sistem, her Document için embedding modeli ile vector oluşturur.
8. Sistem, embedding'leri VectorStore'a kaydeder.
9. Ingestion pipeline tamamlanır.

---

## Extensions

### 3a. Doküman bulunamadı:
- **3a1.** Sistem, FileNotFoundException loglar.
- **3a2.** Pipeline, boş doküman listesi ile devam eder.

### 4a. Docling Serve erişilemez:
- **4a1.** Sistem, connection error loglar.
- **4a2.** Application startup başarısız olur.
- **4a3.** Health check "docling: DOWN" gösterir.

### 5a. Parsing hatası:
- **5a1.** Docling Serve, parsing error döner.
- **5a2.** Sistem, hatayı loglar ve ilgili dokümanı atlar.
- **5a3.** Diğer dokümanlarla devam eder.

### 7a. OpenAI API erişilemez:
- **7a1.** OpenAI API hatası döner (rate limit, timeout, vs).
- **7a2.** Sistem, hatayı loglar ve retry with exponential backoff uygular.
- **7a3.** Başarısız olursa pipeline durur.

### 8a. Vector store bağlantı hatası:
- **8a1.** Sistem, database connection error loglar.
- **8a2.** Pipeline başarısız olur.
- **8a3.** Application startup başarısız olur.

### 8b. Duplicate document:
- **8b1.** Sistem, mevcut embedding'leri algılar.
- **8b2.** Yeni embedding'ler eklenir (upsert değil insert).

---

## Variations

### 1. Doküman Kaynağı:
- a) Classpath resources (`documents/*.pdf`)
- b) External file system
- c) HTTP URLs

### 2. Chunking Strategy:
- a) Hierarchical chunking (default - Docling)
- b) Fixed size chunking
- c) Semantic chunking

### 3. Trigger:
- a) Application startup (`@PostConstruct`)
- b) Scheduled job
- c) On-demand API call

---

## API Endpoints

### POST /api/documents/ingest
Dokümanı convert edip vector store'a ingest eder.

**Request Body:**
```json
{
  "url": "https://example.com/document.pdf"
}
```
veya
```json
{
  "filePath": "/path/to/document.pdf"
}
```

**Response (Success):**
```json
{
  "success": true,
  "documentCount": 5,
  "message": "Successfully ingested 5 document chunks"
}
```

**Response (Failure):**
```json
{
  "success": false,
  "documentCount": 0,
  "message": "Failed to convert document: Connection timeout"
}
```

### GET /api/documents/ingest/http
URL'den doküman ingest eder.

**Query Parameters:**
- `url` (required): HTTP URL of the document

**Example:** `GET /api/documents/ingest/http?url=https://example.com/doc.pdf`

### GET /api/documents/ingest/file
Dosya yolundan doküman ingest eder.

**Query Parameters:**
- `path` (required): File system path to the document

**Example:** `GET /api/documents/ingest/file?path=/data/docs/report.pdf`

---

## Technical Notes

### Feature Location
- **Feature:** `document`
- **Use Case:** `document/application/IngestDocumentUseCase.java`
- **Pipeline:** `config/IngestionPipeline.java`

### Component Structure
```java
@Component
class IngestionPipeline {
    private final DoclingServeApi doclingServeApi;
    private final VectorStore vectorStore;

    @PostConstruct
    void run() {
        Resource file = new ClassPathResource("documents/story.pdf");

        List<Document> documents = DoclingDocumentReader.builder()
                .doclingServeApi(doclingServeApi)
                .files(file)
                .build()
                .get();

        vectorStore.add(documents);
    }
}
```

### Vector Store Configuration
```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        dimensions: 1536
        index-type: hnsw
        initialize-schema: true
```

### Embedding Model
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      embedding:
        model: text-embedding-3-small
```

---

## Data Flow Diagram

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Documents  │───▶│ DoclingDocReader │───▶│  Docling Serve  │
│  (PDF/DOCX) │    │   (Spring AI)    │    │   (Container)   │
└─────────────┘    └──────────────────┘    └────────┬────────┘
                                                     │
                                                     ▼
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  PGVector   │◀───│   VectorStore    │◀───│ Chunked Docs    │
│  (Database) │    │   (Spring AI)    │    │ + Embeddings    │
└─────────────┘    └──────────────────┘    └─────────────────┘
```

---

## Metrics & Monitoring

| Metric | Description |
|--------|-------------|
| `ingestion.documents.count` | İşlenen doküman sayısı |
| `ingestion.chunks.count` | Oluşturulan chunk sayısı |
| `ingestion.embeddings.count` | Kaydedilen embedding sayısı |
| `ingestion.duration` | Pipeline süresi (ms) |
| `vectorstore.size` | Vector store'daki toplam kayıt |