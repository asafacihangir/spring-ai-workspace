# Proje Amacı — Spring AI Semantic Caching

## Ana Hedef

Bir Spring AI sohbet uygulamasına **semantic caching** ekleyerek, kullanıcılar **farklı kelimelerle de olsa aynı anlama gelen** bir soruyu tekrar sorduğunda yanıtı **LLM'i yeniden çağırmadan** cache'ten döndürmek. Böylece hem gecikme hem de model maliyeti düşürülür.

## Çözülen Problem

Geleneksel cache'ler yalnızca **birebir aynı** istek tekrarlandığında çalışır. İki soru aynı anlama gelse de farklı kelimelerle yazıldığında klasik cache ıskalar ve LLM gereksiz yere yeniden çağrılır:

- "What is the height requirement for Big Thunder Mountain in Disneyland?"
- "How tall must I be to ride Big Thunder Mountain in Disneyland?"

Bu iki soru metin olarak farklı ama anlamca eşdeğerdir. Metin tabanlı cache ikisini ayrı sayar; LLM iki kez çağrılır.

## Çözüm Yaklaşımı

Önceki istekler için **embedding** (vector temsili) saklanır. Yeni bir soru geldiğinde onun embedding'i üretilir ve saklanan embedding'lerle **vector similarity** karşılaştırması yapılır:

- Similarity, yapılandırılan **threshold**'u aşıyorsa → cache'teki yanıt doğrudan döner, LLM çağrılmaz.
- Threshold'un altındaysa → LLM çağrılır, yanıt üretilir ve sonraki istekler için cache'e yazılır.

Bu yaklaşım, cache'i **metin tabanlı** olmaktan çıkarıp **semantic** hâle getirir.

## Hedef Kullanıcı

Spring AI ile sohbet uygulaması geliştiren ve tekrar eden/benzer sorularda LLM gecikme ve maliyetini azaltmak isteyen Java geliştiricileri.

## Tasarım Felsefesi

- **Semantic isabet:** Birebir metin değil, anlamca yakınlık (semantic similarity) cache hit'i belirler.
- **Şeffaf entegrasyon:** Cache, `ChatClient` akışına bir advisor olarak takılır; çağrı kodu değişmeden tüm istekler cache'e katılır.
- **Yapılandırılabilir hassasiyet:** Similarity threshold dışarıdan ayarlanabilir; yüksek değer daha sıkı eşleşme, düşük değer daha çok isabet demektir.

## Başarı Kriteri

Kullanıcı bir soruyu sorduktan sonra, **anlamca eşdeğer** ama **farklı kelimelerle yazılmış** ikinci bir soru sorduğunda; aynı yanıtı **LLM yeniden çağrılmadan** ve **belirgin biçimde daha hızlı** alabiliyor.
