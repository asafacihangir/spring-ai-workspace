# Spring AI + Docling RAG System

Kurumsal doküman işleme ve **Retrieval Augmented Generation (RAG)** yetenekleri sunan örnek uygulama. Doküman parsing on-premises (**Docling**) çalışır; sadece soru-cevap için **OpenAI** kullanılır.

- **On-premises parsing:** PDF, DOCX, presentation dosyaları lokal işlenir, gizlilik korunur.
- **RAG:** Dokümanlar chunk'lanır, embed edilir, PGVector'a yazılır; sorular kaynak içeriğe dayanarak yanıtlanır.
- **Hallucination azaltma:** Yanıtlar yalnızca ingest edilmiş dokümanlardan üretilir.

## Teknoloji Stack'i

| Katman | Teknoloji |
|--------|-----------|
| Dil / Framework | Java 25, Spring Boot 3.5+, Spring AI 1.1.2+ |
| Doküman işleme | Docling (IBM Research) + Arconia 0.20.0 |
| LLM | OpenAI GPT-4o (chat), text-embedding-3-small (1536 dim) |
| Vektör DB | PostgreSQL 18 + PGVector (HNSW) |
| Build | Maven 3.9+ |

Mimari **feature-based layered** (document / chat / health), her feature `api` / `application` / `domain` / `infrastructure` katmanlarına ayrılır. Detay: [`docs/nfr_techstack.md`](docs/nfr_techstack.md).

## Gereksinimler

- JDK 25+
- Docker veya Podman (Docling + PostgreSQL container'ları için)
- Geçerli bir OpenAI API key

## Kurulum

```bash
cp .env.example .env   # OPENAI_API_KEY değerini doldurun
```

### Geliştirme (Dev Services container'ları otomatik başlatır)

```bash
./mvnw spring-boot:run
```

### Docker Compose ile (Task)

```bash
task start      # infra + uygulamayı ayağa kaldırır
task stop       # durdurur
task restart
```

## API

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `GET` | `/api/documents/convert/http?url={url}` | URL'den doküman dönüştür |
| `POST` | `/api/documents/convert` | Request body ile dönüştür |
| `GET` | `/api/documents/ingest/http?url={url}` | URL'den ingest et |
| `POST` | `/api/documents/ingest` | Request body ile ingest et |
| `POST` | `/api/chat` | RAG sorgusu gönder |
| `GET` | `/actuator/health` | Sistem sağlık durumu |

## Ortam Değişkenleri

| Değişken | Zorunlu | Varsayılan |
|----------|---------|------------|
| `OPENAI_API_KEY` | Evet | - |
| `POSTGRES_HOST` | Hayır | localhost |
| `POSTGRES_PORT` | Hayır | 15432 |
| `POSTGRES_DB` | Hayır | vectordb |
| `DOCLING_URL` | Hayır | http://localhost:5001 |

## Dokümantasyon

| Doküman | İçerik |
|---------|--------|
| [`docs/project_context.md`](docs/project_context.md) | Proje bağlamı, ADR'ler, kapsam |
| [`docs/nfr_techstack.md`](docs/nfr_techstack.md) | Teknoloji stack & mimari |
| [`docs/architecture_rules.md`](docs/architecture_rules.md) | Mimari kurallar |
| [`docs/coding_standards.md`](docs/coding_standards.md) | Kodlama standartları |
| [`docs/01-business_requirements/`](docs/01-business_requirements/) | Fonksiyonel gereksinimler |
| [`docs/02-usecases/`](docs/02-usecases/) | Use case'ler |
| [`docs/03-non_functional/`](docs/03-non_functional/) | Non-functional requirements |
| [`test-scenarios.md`](test-scenarios.md) | Manuel test senaryoları |