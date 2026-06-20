# todowritetool

`todowritetool`, karmaşık bir görevi alan, önce planlayan, ardından adımları sırayla tamamlayan bir Spring AI agent örneğidir.

Kullanıcı yalnızca hedefi söyler.

Agent ise:

```text
Planlar
  ↓
Bir adımı başlatır
  ↓
Tamamlar
  ↓
Sonraki adıma geçer
  ↓
%100 olduğunda cevabı döner
```

Agent, kendisine verilen görevi doğrudan cevaplamaya çalışmaz. Önce bir yapılacaklar listesi oluşturur, ardından listedeki işleri tek tek tamamlar.

---

## Kullanılan teknolojiler

* Java 21
* Spring Boot 4.1
* Spring AI 2.0
* `spring-ai-agent-utils`-`TodoWriteTool`
* OpenAI API

---

## Nasıl çalışır?

### Örnek senaryo

Bir kullanıcı, yapay zekâya şu görevi verdi:

> Tom Hanks’in en iyi 10 filmini bul, film isimlerini ters çevir ve ikili gruplara ayır.

Bu, tek cümlelik bir istek gibi görünse de aslında birden fazla adımdan oluşuyordu:

1. Filmleri bulmak
2. Film isimlerini ters çevirmek
3. Sonuçları ikili gruplara ayırmak
4. Düzenli bir çıktı oluşturmak

`todowritetool`, tam olarak bu tür görevler için hazırlanmış basit bir agent uygulamasıdır.

### İstek nasıl işlenir?

Kullanıcı `/agent` endpoint’ine bir mesaj gönderir. İstek önce `AgentController` tarafından karşılanır, `AgentService` sınıfına iletilir ve görev Spring AI `ChatClient` üzerinden modele gönderilir.

```text
POST /agent
  ↓
AgentController
  ↓
AgentService
  ↓
ChatClient
  ↓
TodoWriteTool kullanan agent
```

### Adım adım ilerleme

Agent hemen nihai cevabı üretmez. Önce `TodoWrite` aracını çağırarak bir plan oluşturur:

```text
☐ Determine top 10 Tom Hanks movies
☐ Reverse each movie title
☐ Group the reversed titles into pairs
☐ Print the grouped reversed titles
— %0
```

Daha sonra ilk görevi çalışmaya başlar:

```text
▶ Determine top 10 Tom Hanks movies
☐ Reverse each movie title
☐ Group the reversed titles into pairs
☐ Print the grouped reversed titles
— %0
```

İlk adım tamamlandığında liste güncellenir:

```text
✓ Determine top 10 Tom Hanks movies
☐ Reverse each movie title
☐ Group the reversed titles into pairs
☐ Print the grouped reversed titles
— %25
```

Agent aynı şekilde listedeki diğer görevleri de sırayla tamamlar.

Aynı anda yalnızca bir görev `in_progress` durumunda olabilir.

Son cevap üretilmeden önce bütün görevlerin tamamlanmış olması gerekir:

```text
✓ Determine top 10 Tom Hanks movies
✓ Reverse each movie title
✓ Group the reversed titles into pairs
✓ Print the grouped reversed titles
— %100
```

Böylece terminal üzerinde agent’ın görevi nasıl ilerlettiği adım adım görülebilir.

### Agent’ın uyması gereken kurallar

Agent’ın system prompt’u, üç veya daha fazla adımdan oluşan görevlerde şu kuralları uygular:

* Cevap üretmeden önce `TodoWrite` ile plan oluştur.
* Aynı anda yalnızca bir görevi `in_progress` yap.
* Tamamlanan görevi hemen `completed` olarak işaretle.
* Bir görev tamamlandıktan sonra sıradaki göreve geç.
* Nihai cevaptan önce bütün görevleri `completed` yapan son bir `TodoWrite` çağrısı yap.

Son kural sayesinde ilerleme her zaman `%100` seviyesine ulaşır.

---

## Kurulum ve çalıştırma

Proje için JDK 21 gerekir.

`.sdkmanrc` dosyasında kullanılan sürüm:

```text
21.0.2-tem
```

Önce OpenAI API anahtarını tanımlayın:

```bash
export OPENAI_API_KEY=sk-...
```

Ardından uygulamayı başlatın:

```bash
./mvnw spring-boot:run
```

Uygulama varsayılan olarak şu adreste çalışır:

```text
http://localhost:8080
```

---

## Kullanım

### İstek

Kullanıcı `/agent` endpoint’ine bir mesaj gönderir:

```http
POST /agent
Content-Type: application/json
```

```json
{
  "message": "Find the top 10 Tom Hanks movies, reverse each title, then group them into pairs"
}
```

`message` alanı eksik veya boş olduğunda:

```http
400 Bad Request
```

döner.

### Yanıt

Agent bütün adımları tamamladıktan sonra `/agent` endpoint’i şu yapıda bir yanıt döner:

```json
{
  "result": "Pair 1:\npmuG tserroF\nnayR etavirP gnivaS\n\nPair 2:\naihpledalihP\nyawA tsaC\n\nPair 3:\n31 ollopA\ngiB\n\nPair 4:\nnaC uoY fI eM hctaC\neliM neerG ehT\n\nPair 5:\nnwO riehT fo eugaeL A\nyrotS yoT"
}
```

İlerleme bilgileri HTTP yanıtının içinde yer almaz. Her `TodoWrite` çağrısı sırasında uygulamanın konsoluna yazılır.

### Örnek istek

```bash
curl -s -X POST http://localhost:8080/agent \
  -H 'Content-Type: application/json' \
  -d '{
    "message": "Find the top 10 Tom Hanks movies, reverse each title, then group them into pairs"
  }'
```

---

## Proje yapısı

Projede her sınıf hikâyenin farklı bir bölümünden sorumludur.

### `ChatClientConfig`

Agent’ın nasıl davranacağını belirler.

Burada:

* Spring AI `ChatClient` oluşturulur.
* `TodoWriteTool` varsayılan araç olarak eklenir.
* Agent’ın çalışma kurallarını içeren system prompt tanımlanır.
* Her todo güncellemesini loglayan `TodoEventHandler` bağlanır.

### `AgentController`

Dış dünyadan gelen istekleri karşılar (`POST /agent`). `message` alanı eksik veya boş olduğunda `400 Bad Request` döner.

### `AgentService`

Controller’dan gelen mesajı `ChatClient` üzerinden agente gönderir ve nihai cevabı döndürür.

Temel akış şöyledir:

```java
chatClient.prompt()
    .user(message)
    .call();
```

### `TodoProgress`

Todo listesini insanların kolayca okuyabileceği bir ilerleme çıktısına dönüştürür.

Durumlar şu sembollerle gösterilir:

```text
☐ pending
▶ in_progress
✓ completed
```

Tamamlanan görev sayısına göre ilerleme yüzdesini hesaplar.

---

## Testler

Testleri çalıştırmak için:

```bash
./mvnw test
```

Projede iki temel test bulunur:

* `TodoProgressTest`: İlerleme formatlayıcısını test eder.
* `contextLoads`: Spring context’inin sorunsuz yüklendiğini doğrular.

Context testi dummy bir API key kullanır ve gerçek bir LLM çağrısı yapmaz. Canlı model davranışı test kapsamına dahil değildir.

---

## Kapsam dışı

Bu proje özellikle küçük ve anlaşılır tutulmuştur. Şu özellikler kapsam dışındadır:

* SSE veya streaming ile canlı ilerleme aktarımı
* Chat memory
* Dosya sistemi araçları
* Shell komutu çalıştırma
* Web araması
* Kalıcı veri depolama
* Kimlik doğrulama

Şu anda ilerleme yalnızca uygulama konsolunda görünür. API istemcisine gerçek zamanlı olarak gönderilmez.