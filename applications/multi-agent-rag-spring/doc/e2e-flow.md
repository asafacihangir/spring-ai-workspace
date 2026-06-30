# Adaptive RAG Sistemi: Uçtan Uca Akış

Bu belge, multi-agent RAG sisteminin tamamını bir hikaye gibi anlatır. Uygulama başlatıldığı andan bir sorunun cevaplanmasına kadar her şeyi adım adım takip edebilirsiniz.

---

## Genel Bakış

Bu sistem, kullanıcının sorduğu soruyu en iyi şekilde cevaplamak için birden fazla yapay zeka ajanını bir arada kullanan bir Spring Boot uygulamasıdır. Soruyu analiz eder, doğru kaynağa yönlendirir, cevap üretir ve ürettiği cevabın kalitesini kontrol eder. Cevap yeterince iyi değilse soruyu yeniden yazar ve tekrar dener.

Sistemde iki ana bilgi kaynağı vardır:
- **Vector Store** — daha önce yüklenmiş belgelerden oluşan yerel bilgi tabanı
- **Web Search** — Tavily API üzerinden yapılan internet araması

```
Kullanıcı Sorusu
       |
       v
 [Soru Yönlendirici]
    /          \
   v            v
Vector Store   Web Arama
   |            |
   v            v
 Cevap Üret   Cevap Üret
   |            |
   v            v
 [Kalite Kontrol]
    /     |      \
   v      v       v
 Tamam  Tekrar   Soruyu
 (END)  Dene     Yeniden Yaz
```

---

## Bölüm 1: Uygulama Ayağa Kalkarken Ne Olur?

Spring Boot uygulaması başladığında arka planda birçok hazırlık yapılır.

### 1.1 Model Ayarları (`application.yaml`)

Uygulama, OpenAI servisini kullanır:
- **Chat model:** `gpt-4o-mini` — soruları cevaplayan, yönlendiren, değerlendiren model
- **Embedding model:** `text-embedding-3-small` — belgeleri ve soruları sayısal vektörlere dönüştüren model
- **Port:** `6666` — uygulama bu port üzerinde çalışır

API key'ler ortam değişkenlerinden (`OPENAI_API_KEY`, `TAVILY_API_KEY`) okunur.

### 1.2 Yedi Farklı ChatClient Yaratılır (`ChatClientConfig`)

Her biri farklı bir görev için özelleşmiş yedi adet ChatClient bean oluşturulur. Hepsi aynı `gpt-4o-mini` modelini kullanır ama her birinin kendi system prompt'u, temperature ayarı ve konuşma hafızası vardır:

| ChatClient | Görevi | Temperature | Neden Bu Temperature? |
|---|---|---|---|
| `openAiChatClient` | Genel amaçlı, RAG retrieval'da kullanılır | 0.8 | Zengin, çeşitli cevaplar için |
| `QuestionRouterChatClient` | Soruyu vector store veya web search'e yönlendirir | 0.0 | Her seferinde tutarlı karar vermeli |
| `WebSearchChatClient` | Tavily tool'unu çağırarak internetten arama yapar | 0.8 | Arama sorguları esnek olabilir |
| `AdaptiveRagChatClient` | Bulunan belgelere bakarak cevap üretir | 0.7 | Yaratıcı ama kontrollü cevap |
| `HallucinationChatClient` | Cevabın belgelere dayalı olup olmadığını kontrol eder | 0.0 | Kesin evet/hayır kararı |
| `AnswerGraderChatClient` | Cevabın soruyu gerçekten çözüp çözmediğini kontrol eder | 0.0 | Kesin evet/hayır kararı |
| `QuestionRewriterChatClient` | Soruyu daha net hale getirerek yeniden yazar | 0.0 | Tutarlı ve odaklı yeniden yazım |

**Temperature nedir?** 0.0 demek "en tahmin edilebilir, en tutarlı cevap" demektir. 0.8 demek "daha yaratıcı, daha çeşitli cevap" demektir. Değerlendirme yapan client'lar 0.0 kullanır çünkü onlardan net karar beklenir. Cevap üreten client'lar 0.7-0.8 kullanır çünkü zengin cevaplar istenir.

Her ChatClient'ın ayrı bir `conversationId` ile kendi konuşma hafızası vardır. Bu sayede yönlendirici, değerlendirici ve cevap üretici birbirinin hafızasını karıştırmaz.

### 1.3 RAG Altyapısı Hazırlanır (`RagConfig`)

- **SimpleVectorStore** oluşturulur. Eğer daha önce kaydedilmiş bir `vectorstore.json` dosyası varsa otomatik olarak yüklenir — böylece uygulama her açıldığında belgeler tekrar embed edilmek zorunda kalmaz.
- **DocumentRetriever** tanımlanır: similarity threshold `0.50` — yani bir belgenin soruyla en az %50 benzerlikte olması gerekir.
- Üç adet **query transformer** oluşturulur. Bunlar, kullanıcının sorusunu vector store'a göndermeden önce iyileştiren ön işleme adımlarıdır:
  1. **CompressionQueryTransformer** — önceki konuşma geçmişini sıkıştırarak soruyu bağımsız hale getirir
  2. **TranslationQueryTransformer** — soruyu İngilizce'ye çevirir (model İngilizce belgelerle daha iyi çalışır)
  3. **RewriteQueryTransformer** — soruyu retrieval için daha uygun bir forma dönüştürür

### 1.4 Graph Oluşturulur (`GraphConfig`)

Bütün node'lar ve edge'ler bir araya getirilerek bir **StateGraph** oluşturulur. Bu graph, sorunun hangi aşamalardan geçeceğini belirleyen bir iş akışı haritasıdır.

Graph'taki dört node:
- `prebuilt_rag_generation` — Vector store'dan belge çekip cevap üretir
- `web_search` — İnternetten arama yapar
- `self_rag_generation` — Bulunan belgelere bakarak cevap üretir
- `transform_query` — Soruyu yeniden yazar

Graph'taki iki karar noktası (edge):
- **RouteQuestionEdge** — Soru hangi kaynağa gitsin?
- **GradeGenerationEdge** — Üretilen cevap yeterince iyi mi?

Uygulama başlarken graph'ın **Mermaid diyagramı** log'a yazdırılır. Bu diyagram sayesinde akışın görsel halini görebilirsiniz.

### 1.5 Graph Derlenir (`GraphController`)

`GraphController` oluşturulurken `StateGraph` bir kez `compile()` edilir ve bir `CompiledGraph` haline gelir. Bu derleme işlemi uygulama başlangıcında bir kez yapılır. Sonraki her istek bu derlenmiş graph üzerinden çalışır.

---

## Bölüm 2: Belgeler Nasıl Yüklenir?

Sisteme soru sormadan önce bilgi tabanını doldurmak gerekir. Bunun için tek yapılması gereken:

```
GET http://localhost:6666/graph/add
```

Bu endpoint çağrıldığında şunlar olur:

1. `src/main/resources/documents/` altındaki `faq.md` ve `overview.md` dosyaları okunur
2. `MarkdownDocumentReader` bu dosyaları `Document` nesnelerine dönüştürür
3. Her belge `text-embedding-3-small` modeli tarafından sayısal vektörlere dönüştürülür (embedding)
4. Bu vektörler `SimpleVectorStore`'a eklenir
5. Vector store `vectorstore.json` dosyasına kaydedilir

Bu işlem bir kez yapılır. Sonraki uygulama başlatmalarında `vectorstore.json` otomatik yüklenir, tekrar embed yapmaya gerek kalmaz.

> Eğer `vectorstore.json` zaten varsa, `/graph/add` çağrısı dosyayı atlayabilir. Bu, tekrarlanan yüklemeleri engeller.

---

## Bölüm 3: Bir Soru Sorulduğunda Ne Olur?

Şimdi hikayemizin ana kısmına geldik. Diyelim ki kullanıcı şu isteği gönderdi:

```
GET http://localhost:6666/graph/chat?query=Spring AI Alibaba nedir?
```

### Adım 3.1: State Oluşturulur

`GraphController.chat()` metodu çağrılır. Yeni bir `OverAllState` oluşturulur ve soruyu içine koyar:

```
State: {
  question: "Spring AI Alibaba nedir?",
  generation: null,
  documents: null,
  retry_count: null
}
```

Her istek için benzersiz bir `threadId` (UUID) atanır. Bu sayede eşzamanlı istekler birbirini etkilemez.

### Adım 3.2: Soru Yönlendirilir (RouteQuestionEdge)

Graph çalışmaya `START` noktasından başlar. İlk iş, sorunun hangi kaynağa yönlendirileceğine karar vermektir.

**RouteQuestionEdge** devreye girer:
1. State'ten `question` değerini okur
2. `QuestionRouterChatClient`'a soruyu ve bilgi tabanı açıklamasını gönderir
3. LLM, `RouteQueryEntity` formatında bir JSON döndürür: `{"data_source": "vectorstore"}` veya `{"data_source": "web_search"}`

**Yönlendirici şuna karar verir:**
- Soru bilgi tabanındaki belgelerle ilgiliyse → `"vectorstore"` (yerel bilgi tabanı)
- Soru gerçek zamanlı bilgi gerektiriyorsa (hava durumu, borsa, güncel haberler) → `"web_search"` (internet araması)
- LLM cevap veremezse → güvenli varsayılan olarak `"web_search"`

Bu örnekte soru "Spring AI Alibaba nedir?" olduğundan ve bilgi tabanında bununla ilgili belgeler bulunduğundan, yönlendirici `"vectorstore"` kararını verir.

---

## Bölüm 4: Vector Store Yolu

Yönlendirici `"vectorstore"` dedi. Şimdi `prebuilt_rag_generation` node'u çalışır.

### Adım 4.1: Belge Çekme ve Cevap Üretme (RetrieveNode)

Bu node, RAG sürecinin tamamını tek seferde yapar — hem belge arar hem cevap üretir.

**Önce soru iyileştirilir (Pre-Retrieval):**

Soru, `RetrievalAugmentationAdvisor` içindeki üç transformer'dan sırayla geçer:

1. **CompressionQueryTransformer** — Eğer önceki konuşmalardan bağlam varsa, soruyu bağımsız hale getirir
   - Örnek: "Peki bunun avantajları nelerdir?" → "Spring AI Alibaba'nın avantajları nelerdir?"

2. **TranslationQueryTransformer** — Soruyu İngilizce'ye çevirir
   - Örnek: "Spring AI Alibaba nedir?" → "What is Spring AI Alibaba?"

3. **RewriteQueryTransformer** — Soruyu arama için daha uygun hale getirir
   - Örnek: "What is Spring AI Alibaba?" → "Spring AI Alibaba framework features capabilities overview"

**Sonra belgeler aranır (Retrieval):**

İyileştirilmiş soru, `VectorStoreDocumentRetriever`'a gönderilir. Soru embedding'e dönüştürülür ve vector store'daki belgelerle karşılaştırılır. Benzerlik skoru %50'nin üzerinde olan belgeler döndürülür.

**Son olarak cevap üretilir (Generation):**

Bulunan belgeler ve soru birlikte `openAiChatClient`'a (temperature 0.8) gönderilir. Model, belgelere dayanarak bir cevap üretir.

State güncellenir:
```
State: {
  question: "Spring AI Alibaba nedir?",
  generation: "Spring AI Alibaba, Spring AI framework'ünü Alibaba Cloud servisleriyle...",
  documents: [Document1, Document2, ...],
  retry_count: null
}
```

---

## Bölüm 5: Web Search Yolu

Eğer yönlendirici `"web_search"` deseydi ne olurdu? Diyelim ki soru "Bugün İstanbul'da hava nasıl?" olsun.

### Adım 5.1: Web Araması (WebSearchNode)

`web_search` node'u çalışır:

1. `WebSearchChatClient`'a soru gönderilir (temperature 0.8)
2. Bu client'ın bir tool'u vardır: `WebSearchTool`
3. LLM, soruyu değerlendirir ve **tool calling** mekanizmasıyla `WebSearchTool.search()` metodunu çağırır
4. `WebSearchTool`, Tavily API'ye bir POST isteği gönderir:
   - `maxResults: 3` — en fazla 3 sonuç döner
   - `searchDepth: "basic"` — temel düzey arama
   - `includeAnswer: true` — Tavily'nin kendi sentez cevabını da ister
5. Tavily'den dönen sonuçlar `Document` nesnelerine dönüştürülür
6. Tavily'nin sentez cevabı varsa, listenin başına eklenir

State güncellenir:
```
State: {
  question: "Bugün İstanbul'da hava nasıl?",
  documents: [TavilySentezCevabı, Sonuç1, Sonuç2, Sonuç3],
  generation: null   // henüz cevap üretilmedi
}
```

### Adım 5.2: Cevap Üretme (GenerationNode)

Web arama yolunda, belge çekme ve cevap üretme ayrı node'larda yapılır. Bir önceki adımda belgeler toplandı, şimdi `self_rag_generation` node'u cevap üretir.

`GenerationNode` çalışır:
1. State'ten `question` ve `documents` değerlerini okur
2. Belgelerin metinlerini birleştirir (`\n\n` ile ayırarak)
3. `AdaptiveRagChatClient`'a (temperature 0.7) soruyu ve birleştirilmiş belge metnini gönderir
4. LLM, belgelere dayanarak bir cevap üretir

State güncellenir:
```
State: {
  question: "Bugün İstanbul'da hava nasıl?",
  documents: [...],
  generation: "İstanbul'da bugün hava parçalı bulutlu ve sıcaklık 15°C civarında..."
}
```

---

## Bölüm 6: Kalite Kontrol (GradeGenerationEdge)

Hangi yoldan gelirse gelsin, cevap üretildikten sonra kalite kontrol başlar. `GradeGenerationEdge` iki aşamalı bir değerlendirme yapar.

### Adım 6.1: Circuit Breaker Kontrolü

Her şeyden önce, `retry_count` kontrol edilir. Eğer deneme sayısı 3'ü aştıysa, sonsuz döngüye girmemek için mevcut cevap kabul edilir ve `"useful"` döndürülür.

Bu bir güvenlik mekanizmasıdır — sistem sonsuza kadar denememeli.

### Adım 6.2: Hallucination Kontrolü

**Soru:** Üretilen cevap, bulunan belgelere gerçekten dayanıyor mu?

`HallucinationChatClient` (temperature 0.0) çağrılır:
- Girdi: bulunan belgeler + üretilen cevap
- Çıktı: `GradeScore` → `{"binary_score": "yes"}` veya `{"binary_score": "no"}`

**"yes" ise:** Cevap belgelere dayanıyor, bir sonraki kontrole geç.

**"no" ise:** Cevap uydurma (hallucination). `retry_count` artırılır ve `"hallucination"` döndürülür. Graph, aynı node'a geri döner ve cevabı tekrar üretir.

### Adım 6.3: Cevap Kalitesi Kontrolü

**Soru:** Üretilen cevap, kullanıcının sorusunu gerçekten çözüyor mu?

`AnswerGraderChatClient` (temperature 0.0) çağrılır:
- Girdi: kullanıcının sorusu + üretilen cevap
- Çıktı: `GradeScore` → `{"binary_score": "yes"}` veya `{"binary_score": "no"}`

**"yes" ise:** Cevap soruyu çözüyor. `"useful"` döndürülür → graph `END`'e gider, cevap kullanıcıya döner.

**"no" ise:** Cevap soruyu çözmüyor. `retry_count` artırılır ve `"unuseful"` döndürülür → soru yeniden yazılır.

### Karar Özeti

```
GradeGenerationEdge kararları:
  retry_count > 3         → "useful"       → END (circuit breaker)
  hallucination = hayır   → "hallucination" → aynı node'a geri dön, tekrar üret
  cevap uygun = evet      → "useful"       → END (başarılı)
  cevap uygun = hayır     → "unuseful"     → soruyu yeniden yaz
```

---

## Bölüm 7: Soru Yeniden Yazma (TransformQueryNode)

Eğer `GradeGenerationEdge` `"unuseful"` verdiyse, soru yeterince iyi değildi demektir. `transform_query` node'u devreye girer.

`TransformQueryNode` çalışır:
1. State'ten mevcut `question` değerini okur
2. `QuestionRewriterChatClient`'a (temperature 0.0) gönderir
3. LLM, soruyu daha net ve arama için daha uygun hale getirir

Örnek:
- Orijinal: "Spring AI nedir?"
- Yeniden yazılmış: "Spring AI framework'ünün temel özellikleri, kullanım alanları ve desteklediği model sağlayıcıları nelerdir?"

State güncellenir:
```
State: {
  question: "Spring AI framework'ünün temel özellikleri...",  // yeni soru
  documents: [...],   // önceki belgeler hâlâ duruyor
  generation: "...",  // önceki cevap hâlâ duruyor
  retry_count: 1
}
```

Yeniden yazılan soru her zaman `self_rag_generation` node'una gönderilir. Bu node `GenerationNode` kullanarak yeni soruyla tekrar cevap üretir. Sonra tekrar kalite kontrolünden geçer.

---

## Bölüm 8: Döngü ve Sonlanma

Sistem aşağıdaki döngüde çalışabilir:

```
Soru → Cevap Üret → Kalite Kontrol → Yetersiz → Soruyu Yeniden Yaz → Cevap Üret → Kalite Kontrol → ...
```

Bu döngünün sonlanma koşulları:

1. **Başarılı cevap:** Hem hallucination kontrolünü hem cevap kalitesi kontrolünü geçerse → `"useful"` → `END`
2. **Circuit breaker:** 3 denemeden sonra ne olursa olsun kabul et → `"useful"` → `END`

En iyi durumda tek denemede biter. En kötü durumda 3 deneme sonra circuit breaker devreye girer.

---

## Bölüm 9: Cevap Kullanıcıya Döner

Graph `END`'e ulaştığında, `compiledGraph.invoke()` çağrısı tamamlanır. `OverAllState`'in son hali bir `Map` olarak HTTP response'a dönüştürülür:

```json
{
  "question": "Spring AI Alibaba nedir?",
  "generation": "Spring AI Alibaba, Spring AI framework'ünü Alibaba Cloud servisleriyle...",
  "documents": [ ... ],
  "retry_count": 1
}
```

Kullanıcı `generation` alanından cevabını okur. `documents` alanından hangi kaynaklara bakıldığını görebilir. `retry_count` alanından kaç deneme yapıldığını anlayabilir.

---

## Bölüm 10: Tüm Akışın Özeti

### Senaryo A: Bilgi Tabanındaki Soru (Mutlu Yol)

```
1. Kullanıcı: "Spring AI Alibaba nedir?"
2. RouteQuestionEdge → "vectorstore"
3. RetrieveNode:
   a. Soru transformer'lardan geçer (compress → translate → rewrite)
   b. Vector store'dan belgeler bulunur
   c. Cevap üretilir
4. GradeGenerationEdge:
   a. Hallucination kontrolü → "yes" (belgeler destekliyor)
   b. Cevap kalitesi kontrolü → "yes" (soruyu çözüyor)
   c. → "useful"
5. END → Cevap kullanıcıya döner
```

### Senaryo B: Güncel Bilgi Gerektiren Soru

```
1. Kullanıcı: "Bugün İstanbul'da hava nasıl?"
2. RouteQuestionEdge → "web_search"
3. WebSearchNode:
   a. LLM, WebSearchTool'u tool calling ile çağırır
   b. Tavily API'den arama sonuçları döner
   c. Sonuçlar Document listesine dönüşür
4. GenerationNode:
   a. Belgelere dayanarak cevap üretilir
5. GradeGenerationEdge:
   a. Hallucination kontrolü → "yes"
   b. Cevap kalitesi kontrolü → "yes"
   c. → "useful"
6. END → Cevap kullanıcıya döner
```

### Senaryo C: Cevap Yetersiz, Soru Yeniden Yazılır

```
1. Kullanıcı: "AI nasıl çalışır?" (çok geniş bir soru)
2. RouteQuestionEdge → "web_search"
3. WebSearchNode → belgeler bulunur
4. GenerationNode → cevap üretilir
5. GradeGenerationEdge:
   a. Hallucination kontrolü → "yes"
   b. Cevap kalitesi kontrolü → "no" (soruyu tam çözmüyor)
   c. → "unuseful", retry_count = 1
6. TransformQueryNode:
   a. "AI nasıl çalışır?" → "Yapay zeka algoritmalarının temel çalışma prensipleri..."
7. GenerationNode → yeni soruyla cevap üretilir
8. GradeGenerationEdge → "useful"
9. END → Cevap kullanıcıya döner
```

### Senaryo D: Hallucination Tespit Edilir

```
1. Kullanıcı soru sorar
2. Cevap üretilir
3. GradeGenerationEdge:
   a. Hallucination kontrolü → "no" (cevap uydurma, belgelere dayanmıyor)
   b. → "hallucination", retry_count = 1
4. Aynı node tekrar çalışır, yeni cevap üretir
5. GradeGenerationEdge tekrar kontrol eder
6. Eğer 3 denemeden sonra hâlâ düzelmezse → circuit breaker → "useful" → END
```

---

## Ek: Proje Yapısı

```
src/main/java/com/ai/demo/
├── SpringAiAlibabaDemoApplication.java     # Uygulama giriş noktası
│
├── config/
│   ├── ChatClientConfig.java               # 7 farklı ChatClient tanımı
│   ├── RagConfig.java                      # Vector store, retriever, transformer'lar
│   └── GraphConfig.java                    # Graph node'ları ve edge'leri birleştirir
│
├── controller/
│   └── GraphController.java                # REST API: /graph/add ve /graph/chat
│
├── edge/
│   ├── RouteQuestionEdge.java              # Karar: vectorstore mi, web search mı?
│   └── GradeGenerationEdge.java            # Karar: cevap yeterli mi?
│
├── node/
│   ├── RetrieveNode.java                   # Vector store'dan belge çek + cevap üret
│   ├── GenerationNode.java                 # Belgelere bakarak cevap üret
│   ├── TransformQueryNode.java             # Soruyu yeniden yaz
│   └── WebSearchNode.java                  # Tavily ile internetten ara
│
├── tool/
│   └── WebSearchTool.java                  # Tavily API client (@Tool)
│
└── entity/
    ├── GradeScore.java                     # {"binary_score": "yes/no"}
    └── RouteQueryEntity.java               # {"data_source": "vectorstore/web_search"}
```

## Ek: State Yönetimi

Tüm node'lar ve edge'ler aynı `OverAllState` üzerinden haberleşir. State'te dört alan vardır:

| Alan | Tür | Kim Yazar? | Kim Okur? |
|---|---|---|---|
| `question` | String | Controller (ilk değer), TransformQueryNode (yeniden yazım) | Tüm node ve edge'ler |
| `generation` | String | RetrieveNode, GenerationNode | GradeGenerationEdge |
| `documents` | List\<Document\> | RetrieveNode, WebSearchNode | GenerationNode, GradeGenerationEdge |
| `retry_count` | Integer | GradeGenerationEdge | GradeGenerationEdge |

Her alan `ReplaceStrategy` kullanır — yeni değer yazıldığında eski değerin üzerine yazılır.

## Ek: API Referansı

| Endpoint | Method | Ne Yapar? |
|---|---|---|
| `/graph/add` | GET | Markdown belgelerini okur, embed eder ve vector store'a kaydeder |
| `/graph/chat?query=...` | GET | Soruyu graph üzerinden işler ve cevabı döndürür |
