# Use Case: UC-003 - RAG Query (Question Answering)

**System:** Spring AI + Docling RAG System

**Primary Actor:** End User / API Consumer

**Goal:** Dokümanlar hakkında doğal dilde soru sorarak, context-aware yanıt almak

**Related Requirements:** FR-012, FR-013, FR-014, FR-015

---

## Preconditions

1. Ingestion pipeline tamamlanmış (UC-002)
2. Vector store'da embedding'ler mevcut
3. OpenAI API erişilebilir (GPT-4o ve text-embedding-3-small için)
4. Geçerli OPENAI_API_KEY mevcut

## Postconditions

1. Kullanıcı sorusu işlenmiş
2. İlgili doküman chunk'ları retrieve edilmiş
3. Context-aware yanıt üretilmiş
4. Yanıt kullanıcıya dönülmüş

---

## Main Success Scenario

1. Actor, `/chat` endpoint'ine doğal dilde soru gönderir.
2. Sistem, soruyu ChatController'a iletir.
3. RetrievalAugmentationAdvisor, soruyu intercept eder.
4. VectorStoreDocumentRetriever, soruyu embedding'e dönüştürür.
5. Sistem, vector store'da semantic search yapar.
6. Sistem, en relevant chunk'ları retrieve eder.
7. Sistem, orijinal soruyu retrieve edilen context ile augment eder.
8. Sistem, augmented prompt'u chat modeline gönderir.
9. Chat modeli, context-aware yanıt üretir.
10. Sistem, yanıtı Actor'a döner.

---

## Extensions

### 4a. Embedding oluşturulamıyor:
- **4a1.** OpenAI API, embedding hatası döner.
- **4a2.** Sistem, 500 Internal Server Error döner.

### 5a. Vector store erişilemez:
- **5a1.** Sistem, database connection hatası alır.
- **5a2.** Sistem, 503 Service Unavailable döner.

### 6a. Hiç relevant chunk bulunamadı:
- **6a1.** Similarity score threshold'u geçen sonuç yok.
- **6a2.** Sistem, boş context ile devam eder.
- **6a3.** Model, genel bilgiye dayalı yanıt üretir (veya "bilmiyorum" der).

### 8a. Chat modeli erişilemez:
- **8a1.** OpenAI API erişilemez veya rate limited.
- **8a2.** Sistem, 503 Service Unavailable döner.

### 9a. Model timeout:
- **9a1.** Yanıt üretimi çok uzun sürüyor.
- **9a2.** Sistem, timeout hatası döner.
- **9a3.** Actor, daha kısa/basit soru ile tekrar dener.

### 9b. Model hallucination:
- **9b1.** Model, context'te olmayan bilgi üretir.
- **9b2.** (Kullanıcı fark ederse) Actor, daha spesifik soru sorar.

---

## Variations

### 1. Soru Tipi:
- a) Factual soru ("What is X?")
- b) Summarization ("Summarize the document")
- c) Comparison ("Compare X and Y")
- d) Extraction ("List all X mentioned")

### 2. Response Format:
- a) Plain text (default)
- b) Markdown formatted
- c) JSON structured

### 3. Context Window:
- a) Top-K results (default: 4)
- b) Similarity threshold based
- c) All matching documents

---

## Technical Notes

### Feature Location
- **Feature:** `chat`
- **Use Case:** `chat/application/ProcessQueryUseCase.java`
- **Controller:** `chat/api/ChatController.java`
- **Infrastructure:** `chat/infrastructure/OpenAIChatAdapter.java`, `chat/infrastructure/PgVectorStoreAdapter.java`

### Controller Structure
```java
@RestController
class ChatController {
    private final ChatClient chatClient;

    ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
            .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .build())
                .build())
            .build();
    }

    @GetMapping("/chat")
    String chat(String question) {
        return chatClient
                .prompt(question)
                .call()
                .content();
    }
}
```

### API Endpoint
```
GET /chat?question={userQuestion}
```

### Example Request/Response
```bash
# Request
http :8080/chat question=="What's Iorek's dream?"

# Response
"Iorek dreams of one day going on an adventure around the
North Pole and seeing the Northern Lights."
```

### Model Configuration
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4o
      embedding:
        model: text-embedding-3-small
```

---

## RAG Flow Diagram

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   User      │───▶│  ChatController  │───▶│   ChatClient    │
│  Question   │    │  (REST API)      │    │   (Spring AI)   │
└─────────────┘    └──────────────────┘    └────────┬────────┘
                                                     │
                                                     ▼
                                           ┌─────────────────┐
                                           │ Retrieval       │
                                           │ Augmentation    │
                                           │ Advisor         │
                                           └────────┬────────┘
                                                     │
                        ┌────────────────────────────┼────────────────────────────┐
                        ▼                            ▼                            ▼
               ┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
               │ Embedding Model │         │ Vector Store    │         │ Chat Model      │
               │ (text-emb-3)    │         │ (PGVector)      │         │ (gpt-4o)        │
               └─────────────────┘         └─────────────────┘         └─────────────────┘
                        │                            │                            │
                        │                            │                            │
                        └────────────────────────────┼────────────────────────────┘
                                                     │
                                                     ▼
                                           ┌─────────────────┐
                                           │  Augmented      │
                                           │  Response       │
                                           └─────────────────┘
```

---

## Metrics & Monitoring

| Metric | Description |
|--------|-------------|
| `rag.query.count` | Toplam sorgu sayısı |
| `rag.query.duration` | Sorgu süresi (ms) |
| `rag.retrieval.count` | Retrieve edilen chunk sayısı |
| `rag.retrieval.similarity` | Ortalama similarity score |
| `rag.generation.tokens` | Üretilen token sayısı |
| `rag.errors` | Hata sayısı |

---

## Quality Attributes

| Attribute | Measure | Target |
|-----------|---------|--------|
| **Relevance** | Retrieved chunks'ın soruyla ilgisi | > 80% precision |
| **Latency** | End-to-end response time | < 3 seconds |
| **Accuracy** | Yanıtın doğruluğu | Hallucination < 5% |
| **Availability** | Uptime | 99.5% |