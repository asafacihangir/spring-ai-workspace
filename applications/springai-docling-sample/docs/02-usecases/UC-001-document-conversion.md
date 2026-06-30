# Use Case: UC-001 - Document Conversion

**System:** Spring AI + Docling Document Processing System

**Primary Actor:** Developer / Application User

**Goal:** Bir dokümanı (PDF, DOCX, presentation) yapılandırılmış formata (Markdown) dönüştürmek

**Related Requirements:** FR-001, FR-002, FR-003, FR-004, FR-005, FR-006

---

## Preconditions

1. Docling Serve servisi çalışır durumda
2. Sistem health check'ten geçmiş durumda
3. Doküman erişilebilir durumda (HTTP URL veya local file)

## Postconditions

1. Doküman başarıyla parse edilmiş
2. Markdown formatında çıktı üretilmiş
3. Tablo ve layout bilgileri korunmuş

---

## Main Success Scenario

1. Actor, sisteme doküman kaynağını belirtir (HTTP URL veya file path).
2. Sistem, doküman kaynağının tipini belirler (HttpSource veya FileSource).
3. Sistem, dokümanı Docling Serve API'ye gönderir.
4. Docling Serve, dokümanı parse eder (layout analysis, table recognition, OCR).
5. Docling Serve, DoclingDocument formatında yanıt döner.
6. Sistem, DoclingDocument'tan Markdown content'i extract eder.
7. Sistem, Markdown içeriği Actor'a döner.

---

## Extensions

### 2a. Geçersiz kaynak tipi:
- **2a1.** Sistem, desteklenmeyen kaynak tipi hatası döner.
- **2a2.** Actor, geçerli bir kaynak tipi ile tekrar dener.

### 3a. Docling Serve erişilemez:
- **3a1.** Sistem, connection timeout hatası döner.
- **3a2.** Sistem, health check endpoint'inde "docling: DOWN" gösterir.
- **3a3.** Actor, Docling Serve durumunu kontrol eder.

### 4a. Doküman formatı desteklenmiyor:
- **4a1.** Docling Serve, unsupported format hatası döner.
- **4a2.** Sistem, hatayı Actor'a iletir.

### 4b. Doküman bozuk veya okunamıyor:
- **4b1.** Docling Serve, parsing error döner.
- **4b2.** Sistem, hatayı Actor'a iletir.

### 5a. Doküman çok büyük:
- **5a1.** Sistem, timeout veya memory hatası döner.
- **5a2.** Actor, daha küçük doküman ile dener.

---

## Variations

### 1. Doküman Kaynağı:
- a) HTTP URL (web'den doküman çekme)
- b) Local file (classpath resource)
- c) Base64 encoded content

### 2. Doküman Formatı:
- a) PDF
- b) DOCX
- c) Presentation (PPTX)

### 3. Çıktı Formatı:
- a) Markdown (default)
- b) DoclingDocument (unified format)

---

## Technical Notes

### Feature Location
- **Feature:** `document`
- **Use Case:** `document/application/ConvertDocumentUseCase.java`
- **Controller:** `document/api/DocumentController.java`

### API Endpoint
```
GET /convert/http?url={documentUrl}
GET /convert/file
```

### Request/Response Example
```java
// HTTP Source
ConvertDocumentRequest request = ConvertDocumentRequest.builder()
    .source(HttpSource.builder().url(URI.create(url)).build())
    .build();

// File Source
ConvertDocumentRequest request = ConvertDocumentRequest.builder()
    .source(FileSource.builder()
        .filename("document.pdf")
        .base64String(base64Content)
        .build())
    .build();

// Response
ConvertDocumentResponse response = doclingServeApi.convertSource(request);
String markdown = response.getDocument().getMarkdownContent();
```

---

## Metrics & Monitoring

| Metric | Description |
|--------|-------------|
| `docling.conversion.count` | Toplam dönüşüm sayısı |
| `docling.conversion.duration` | Dönüşüm süresi (ms) |
| `docling.conversion.errors` | Hata sayısı |
| `docling.health.status` | Servis durumu (UP/DOWN) |