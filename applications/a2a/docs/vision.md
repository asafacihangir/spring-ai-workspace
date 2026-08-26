devtasa

# Proje Önerisi: Yazılım Değişiklik Etki Analizi Asistanı

> Bu doküman, "Yazılım Değişiklik Etki Analizi Asistanı" proje önerisine ait ham
> taslak notların düzenlenmiş hâlidir. İçerik birebir korunmuş; cümleler
> tamamlanıp metin Markdown yapısına dökülmüştür.

## Amaç

Gerçek dünyadaki amacı: bir feature isteği veya bug kaydı geldiğinde ilgili
repository'leri incelemek, değişikliğin etkilediği bileşenleri bulmak ve
geliştiriciye uygulanabilir bir çalışma planı hazırlamak.

Örnek kullanıcı isteği:

> "Kullanıcı e-posta adresini değiştirdiğinde tüm aktif oturumları kapatmak
> istiyoruz. Hangi servisler değişmeli, riskleri ve test senaryolarını çıkar."

Bu senaryo, mikroservis projelerinde doğrudan karşılığı olan bir yazılım
geliştirme sürecidir.

## Ajanlar

### 1. EngineeringCoordinatorAgent

Ana ajan olarak:

- Ticket veya feature talebini yorumlar.
- İlgili uzman ajanlara görev dağıtır.
- Sonuçlar arasındaki çelişkileri belirler.
- Birleştirilmiş etki analizi ve uygulama planı üretir.

Ana ajan, kod hakkında tahmin yürütmek yerine bilgiyi ilgili repository
ajanından ister.

### 2. RepositoryAnalysisAgent

A2A üzerinden çalışan uzman ajandır:

- İlgili sınıf ve metotları bulur.
- API, servis ve veri tabanı akışını çıkarır.
- Değişiklikten etkilenecek dosyaları belirler.
- Servisler arası bağımlılıkları raporlar.

Agent Card açıklaması:

> Analyzes source-code repositories to identify affected files,
> execution flows, dependencies and integration boundaries.

Birden fazla repository varsa her repository için ayrı ajan çalıştırılabilir:

- IdentityServiceAgent
- NotificationServiceAgent
- WebClientAgent
- MobileClientAgent

Böylece ana ajan, ticket'ın içeriğine göre doğru repository ajanlarını seçer.

### 3. TestPlanningAgent

Değişiklik için test kapsamı üretir:

- Unit test senaryolarını çıkarır.
- Integration testlerini belirler.
- API sözleşmesi değişikliklerini kontrol eder.
- Regresyon alanlarını listeler.
- Negatif ve edge-case senaryoları üretir.

Agent Card açıklaması:

> Creates risk-based unit, integration, contract and regression
> test plans for proposed software changes.

### 4. SecurityReviewAgent

Her değişiklikte çağrılmak zorunda değildir. Kimlik, yetki, kişisel veri veya
dış girdiler etkilendiğinde devreye girer:

- Yetkilendirme risklerini inceler.
- Veri sızıntısı ihtimalini değerlendirir.
- Yeni saldırı yüzeylerini belirler.
- Loglarda hassas veri bulunup bulunmadığını kontrol eder.

Agent Card açıklaması:

> Reviews proposed software changes for authentication,
> authorization, data exposure and input-validation risks.

Bu ajan sayesinde TaskTool ile koşula bağlı delegasyon pratiği yapılabilir.

## Uzun Süreli Hafıza

Ajanın zaman içinde öğrenebileceği kalıcı proje bilgileri:

- Projenin mimari yaklaşımı
- Takımın kodlama kuralları
- Test isimlendirme standardı
- Kritik modüller
- Deploy ve branch politikaları
- Kullanılmaması gereken eski API'ler
- Takımın aldığı mimari kararlar
- Her repository'nin sorumluluk sınırı

Örnek konuşma:

> Identity Service kullanıcı kimliği ve oturumların tek sahibidir.
> Notification Service doğrudan kullanıcı tablosuna erişmemelidir.
> API değişikliklerinde backward compatibility korunmalıdır.
> Authentication değişikliklerinde contract test zorunludur.

Daha sonraki bir istekte ajan bu kuralları hatırlar:

> Kullanıcı e-posta adresini güncellediğinde oturumları kapatalım.

Ajan, kalıcı proje kararlarına dayanarak oturum kapatma sorumluluğunu Identity
Service içine yerleştirir.

Hafızaya alınmaması gerekenler:

- Mevcut build'in durumu
- Açık pull request listesi
- Son commit kimliği
- Geçici test hatası
- Güncel branch içeriği
- Bir ticket'ın anlık durumu

Bunlar değişken verilerdir ve her analizde kaynak sistemden yeniden alınmalıdır.

## Örnek Akış

```text
Feature/Ticket
      ↓
EngineeringCoordinatorAgent
      ├── IdentityServiceAgent
      ├── NotificationServiceAgent
      ├── WebClientAgent
      ├── SecurityReviewAgent
      └── TestPlanningAgent
      ↓
Birleştirilmiş değişiklik etki analizi
```

Ana ajan önce repository ajanlarından teknik kanıt toplar. Ardından güvenlik
ve test ajanlarına gerçek bulguları gönderir.

## Beklenen Çıktı

```markdown
# Değişiklik Etki Analizi

## İstek

Kullanıcı e-posta adresini değiştirdiğinde bütün aktif
oturumların sonlandırılması.

## Etkilenen bileşenler

- Identity Service
    - UserProfileService
    - SessionService
    - TokenRepository
- Web Client
    - ProfileSettingsPage
    - AuthenticationStore

## Önerilen akış

1. Kullanıcının e-posta güncelleme isteği doğrulanır.
2. E-posta adresi transaction içinde güncellenir.
3. Kullanıcının refresh token kayıtları iptal edilir.
4. Güvenlik olayı audit loguna yazılır.
5. Mevcut istemci yeniden giriş ekranına yönlendirilir.

## Riskler

- Veri tabanı güncellenip token iptali başarısız olabilir.
- Diğer cihazlardaki erişim tokenları süreleri dolana kadar çalışabilir.
- Eski istemciler yeni hata kodunu tanımayabilir.

## Test planı

- Başarılı e-posta değişikliğinden sonra refresh token kullanılamamalı.
- Başarısız e-posta değişikliğinde oturumlar korunmalı.
- Birden fazla cihazdaki bütün oturumlar kapatılmalı.
- Eski istemci sürümleri beklenmeyen cevap almamalı.

## Açık kararlar

- Mevcut cihazın oturumu da kapatılacak mı?
- Access token iptali için deny-list kullanılacak mı?
- Bildirim e-postası eski adrese mi, yeni adrese mi gönderilecek?
```

## MVP Kapsamı

İlk sürümde GitHub, Jira veya gerçek bir vektör veritabanı eklemene gerek yok.
Kontrollü ilerleyebilirsin:

1. İki küçük örnek repository hazırla.
2. Her repository'yi ayrı A2A ajanıyla sun.
3. Ana ajanı TaskTool ile bu ajanlara bağla.
4. Proje kurallarını AutoMemoryToolsAdvisor ile sakla.
5. Ana ajanın yalnızca ilgili repository ajanlarını seçmesini sağla.
6. Sonucu standart bir Markdown etki analizi olarak üret.

## Başarı Kriterleri

- Ana ajan, ticket'a göre doğru repository ajanını seçiyor.
- Kodda bulunmayan sınıf veya dosyaları uydurmuyor.
- Her teknik bulgu için dosya yolu veya sembol gösteriliyor.
- Güvenlik ajanı yalnızca güvenlik açısından anlamlı değişikliklerde çağrılıyor.
- Test planı gerçek etki analizinden sonra hazırlanıyor.
- Kalıcı mimari kararlarla güncel repository içeriği birbirine karıştırılmıyor.
- Ajanlar çeliştiğinde ana ajan bunu gizlemeyip açık karar olarak raporluyor.
- Sistem doğrudan kod değiştirmiyor; önce insan tarafından incelenebilecek bir
  plan üretiyor.

## Kapanış

Bu proje, üç yazıdaki konuları doğal biçimde birleştirir: repository uzmanları
A2A ile servis olarak sunulur, koordinatör onları TaskTool ile çağırır ve
takımın kalıcı mimari kuralları uzun süreli hafızada tutulur.
