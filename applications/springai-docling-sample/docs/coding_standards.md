# Coding Standards - Clean Code Principles

Bu doküman, projedeki kod kalitesi standartlarını ve Clean Code prensiplerini tanımlar.

---

## Temel Kurallar (Özet)

1. **İsimlendirme:** Değişken ve fonksiyon isimleri açıklayıcı olmalı, kısaltma kullanılmamalıdır (örn: `usr` yerine `user`, `getDat` yerine `fetchUserData`).
2. **Fonksiyonlar:** Her fonksiyon tek bir işi yapmalıdır (Single Responsibility Principle). Fonksiyonlar 20-30 satırı geçmemelidir.
3. **SOLID Prensipleri:**
   - Sınıflar değişime kapalı, gelişime açık olmalıdır (Open/Closed).
   - Bağımlılıklar (Dependencies) interface üzerinden enjekte edilmelidir.
4. **Hata Yönetimi:** Try-catch blokları mantıklı kullanılmalı, hatalar yutulmamalı (swallow), loglanmalı veya fırlatılmalıdır.
5. **Yorum Satırları:** "Ne" yapıldığını değil, "neden" yapıldığını açıklayan yorumlar eklenmelidir. Kendi kendini açıklayan kod (Self-documenting code) önceliklidir.
6. **Guard Clauses:** İç içe if/else blokları yerine "Early Return" (Erken dönüş) pattern'i kullanılmalıdır.

---

## 1. Core Principles

### 1.1 Okunabilirlik (Readability)

> **"Use descriptive naming conventions, avoid abbreviations."**

| Kural | Kötü Örnek | İyi Örnek |
|-------|------------|-----------|
| Anlamlı isimler kullan | `d`, `str`, `tmp` | `document`, `searchQuery`, `tempFile` |
| Kısaltmalardan kaçın | `docProc`, `usrQry` | `documentProcessor`, `userQuery` |
| Boolean için soru formu | `flag`, `status` | `isProcessed`, `hasEmbedding` |
| Metotlar fiil ile başlamalı | `document()`, `chunk()` | `convertDocument()`, `createChunk()` |
| Collection'lar çoğul olmalı | `chunkList`, `docArray` | `chunks`, `documents` |

### 1.2 Sadelik (Simplicity - KISS)

> **"Apply KISS principle, keep functions small and focused."**

| Kural | Açıklama |
|-------|----------|
| Tek sorumluluk | Her metot tek bir iş yapmalı |
| Küçük metotlar | Maksimum 20 satır hedefle |
| Sığ nesting | Maksimum 2 seviye iç içe blok |
| Early return | Karmaşık koşullar yerine erken dönüş |

---

## 2. Hata Yönetimi

| Kural | Açıklama |
|-------|----------|
| Hataları yutma | Catch bloğunda mutlaka logla veya fırlat |
| Spesifik exception | Genel `Exception` yerine spesifik exception kullan |
| Anlamlı mesajlar | Exception mesajları bağlam içersin |
| Early validation | Girdileri metot başında doğrula |
| Null handling | `Optional` veya null-check ile güvenli erişim sağla |

---

## 3. Refactoring Kriterleri

Aşağıdaki durumlar tespit edildiğinde refactoring yapılmalıdır:

| Durum | Aksiyon |
|-------|---------|
| Metot 50+ satır | Küçük metotlara böl |
| Nesting 3+ seviye | Early return uygula |
| Karmaşık koşul ifadesi | Ayrı metoda çıkar |
| Tekrar eden kod bloğu | Utility metoda taşı |
| Magic number/string | Constant olarak tanımla |

---

*Son güncelleme: 2024-12-25*
