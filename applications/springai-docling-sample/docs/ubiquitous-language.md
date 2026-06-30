# Ubiquitous Language - Spring AI + Docling RAG System

> **Son Guncelleme:** 2025-12-26

Bu dokuman, proje genelinde tutarli terminoloji kullanimi icin referans sozluk gorevi gorur. Tum ekip uyeleri ve dokumantasyon bu terimleri ayni anlamda kullanmalidir.

---

## Document Processing Domain

| Terim | Tanim | Ornek |
|-------|-------|-------|
| **Docling** | IBM Research tarafindan gelistirilen acik kaynak dokuman parsing cozumu. MIT lisansli, on-premises calisir. | Docling Serve container'i ile PDF parse edilir |
| **Docling Serve** | Docling'in HTTP API wrapper'i. Container olarak calisir ve REST endpoint'leri sunar. | `http://localhost:5001` |
| **DoclingDocument** | Docling'in unified output formati. Tum dokuman turlerini metadata kaybetmeden temsil eder. | PDF, DOCX, PPTX hepsi ayni formata donusur |
| **Document Source** | Islenecek dokumanin kaynagi. HTTP URL veya dosya sistemi yolu olabilir. | `HttpSource`, `FileSource` |
| **Conversion** | Bir dokumanin (PDF, DOCX) Markdown formatina donusturulmesi islemi. | PDF -> Markdown |
| **Parsing** | Dokumandaki yapi, tablo, metin ve goruntulerin ayiklanmasi. | Tablo extraction, OCR |
| **OCR** | Optical Character Recognition - goruntulerdeki metnin dijital metne donusturulmesi. | Taranmis PDF'lerdeki metin |

---

## RAG (Retrieval Augmented Generation) Domain

| Terim | Tanim | Ornek |
|-------|-------|-------|
| **RAG** | Retrieval Augmented Generation - LLM yanitlarini harici bilgi kaynaklariyla zenginlestirme teknigi. | Dokumandan context alarak soru cevaplama |
| **Chunk** | Bir dokumanin daha kucuk, islenebilir parcalara bolunmus hali. Embedding olusturmak icin kullanilir. | 500 kelimelik metin parcasi |
| **Chunking** | Dokumani chunk'lara bolme islemi. Hierarchical, fixed-size veya semantic olabilir. | Hierarchical chunking (Docling default) |
| **Embedding** | Metin verisinin sayisal vector temsili. Semantic similarity hesaplamak icin kullanilir. | 1536 boyutlu float array |
| **Vector** | Embedding'in matematiksel temsili. High-dimensional uzayda bir nokta. | `[0.123, -0.456, 0.789, ...]` |
| **Ingestion** | Dokumanlarin parse edilip, chunk'lara ayrilip, embedding'lere donusturulup vector store'a kaydedilmesi sureci. | Document Ingestion Pipeline |
| **Ingestion Pipeline** | Dokumanlarin yuklenmesi, islenmesi ve saklanmasi sureci (ETL benzeri). | `IngestDocumentUseCase` |
| **Semantic Search** | Anlam tabanli benzerlik aramasi. Keyword matching yerine embedding similarity kullanir. | "AI nedir?" sorusu "yapay zeka" iceren chunk'lari bulur |
| **Similarity Score** | Iki embedding arasindaki benzerlik olcusu. Cosine similarity ile hesaplanir. | 0.85 (0-1 arasi, 1 = ayni) |
| **Retrieval** | Kullanici sorusuna en benzer chunk'larin vector store'dan getirilmesi. | Top-K retrieval (varsayilan K=4) |
| **Augmentation** | Retrieve edilen context'in orijinal soruya eklenmesi. | Soru + ilgili chunk'lar -> augmented prompt |
| **Generation** | LLM'in augmented prompt'a dayali yanit uretmesi. | GPT-4o ile yanit olusturma |
| **Hallucination** | LLM'in kaynak veride olmayan bilgiyi uretmesi. RAG ile azaltilir. | Dokumanla celisen yanit |
| **Context Window** | Retrieve edilen chunk'larin prompt'a dahil edilen kismi. Token limiti var. | 4 chunk, ~2000 token |

---

## Infrastructure Domain

| Terim | Tanim | Ornek |
|-------|-------|-------|
| **Vector Store** | Embedding'lerin depolandigi ve similarity search yapilan veritabani. | PostgreSQL + PGVector |
| **PGVector** | PostgreSQL icin vector storage extension'i. HNSW index destekler. | `spring.ai.vectorstore.pgvector` |
| **HNSW** | Hierarchical Navigable Small World - verimli semantic search icin graph-based index algoritmasi. | PGVector'de varsayilan index tipi |
| **Dev Services** | Development ortaminda otomatik infrastructure provisioning mekanizmasi. | Testcontainers ile PostgreSQL baslatma |
| **Health Check** | Sistemin saglik durumunu kontrol eden mekanizma. | `/actuator/health` |
| **Health Indicator** | Belirli bir component'in saglik durumunu raporlayan sinif. | `DoclingServeHealthIndicator` |

---

## API & Architecture Domain

| Terim | Tanim | Ornek |
|-------|-------|-------|
| **Advisor Pattern** | Spring AI'da request/response akisini modify eden pattern. | `RetrievalAugmentationAdvisor` |
| **ChatClient** | Spring AI'nin chat modelleriyle iletisim icin kullandigi istemci. | `ChatClient.Builder` ile olusturulur |
| **Use Case** | Bir is gereksinimini karsilayan uygulama katmani servisi. | `ProcessQueryUseCase`, `IngestDocumentUseCase` |
| **Domain Model** | Is alani kavramlarini temsil eden value object veya entity. | `UserQuestion`, `ChatResponse`, `DocumentSource` |
| **Controller** | REST API endpoint'lerini sunan katman. | `ChatController`, `ConversionController` |
| **Feature** | Bagimsiz bir is yetenegi. Kendi domain, application ve api katmanlarina sahiptir. | `document`, `chat` |

---

## AI/ML Domain

| Terim | Tanim | Ornek |
|-------|-------|-------|
| **LLM** | Large Language Model - buyuk dil modeli. Metin uretimi ve anlama icin kullanilir. | GPT-4o |
| **Chat Model** | Konusma tabanli metin uretimi yapan LLM. | `gpt-4o` |
| **Embedding Model** | Metni vector'e donusturen model. | `text-embedding-3-small` |
| **Token** | LLM'in isleme birimi. Kelime veya kelime parcasi olabilir. | "Hello" = 1 token, "understanding" = 2 token |
| **Prompt** | LLM'e gonderilen giris metni. | "What is Docling?" |
| **Augmented Prompt** | Context ile zenginlestirilmis prompt. | Soru + retrieve edilen chunk'lar |
| **Inference** | Egitilmis modelin yeni veriler uzerinde tahmin yapmasi. | GPT-4o'dan yanit alma |

---

## Kisaltmalar

| Kisaltma | Acilim | Tanim |
|----------|--------|-------|
| **RAG** | Retrieval Augmented Generation | Context-aware LLM yaniti |
| **LLM** | Large Language Model | Buyuk dil modeli |
| **OCR** | Optical Character Recognition | Goruntuden metin cikarma |
| **HNSW** | Hierarchical Navigable Small World | Vector index algoritmasi |
| **API** | Application Programming Interface | Programatik arayuz |
| **REST** | Representational State Transfer | Web API mimarisi |
| **ETL** | Extract, Transform, Load | Veri isleme pattern'i |
| **SRP** | Single Responsibility Principle | SOLID prensibi |

---

## Iliskili Kavramlar

```
Document Processing Flow:
Document Source -> Docling Serve -> Parsing -> DoclingDocument -> Markdown

Ingestion Flow:
Document -> Chunking -> Embedding -> Vector Store

RAG Query Flow:
User Question -> Embedding -> Semantic Search -> Retrieval -> Augmentation -> Generation -> Response

Component Relationships:
Controller -> Use Case -> Domain Model
             -> External Service (Docling, OpenAI, VectorStore)
```

---

## Iliskili Dokumanlar

| Dokuman | Icerik |
|---------|--------|
| `project_context.md` | Proje baglami ve tech stack |
| `architecture_rules.md` | Mimari kurallar |
| `coding_standards.md` | Kodlama standartlari |
| `01-business-req.md` | Fonksiyonel gereksinimler |
| `UC-001` - `UC-004` | Use case dokumanlari |

---

## Document History

| Versiyon | Tarih | Degisiklikler |
|----------|-------|---------------|
| 1.0 | 2025-12-26 | Ilk versiyon |
