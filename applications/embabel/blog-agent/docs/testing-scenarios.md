# Blog Agent — Uctan Uca Test Senaryolari

Bu dosya, blog-agent projesinin tum katmanlarini kapsayan manuel test adimlaridir.
Her katman icin ayri bir bolum vardir. Testleri sirayla calistirin.

## On Kosullar

- Java 23 yuklu (`java -version` ile kontrol et)
- `OPENAI_API_KEY` environment variable tanimli (`echo $OPENAI_API_KEY` ile kontrol et)
- Terminal'de `blog-agent/` dizininde olun

```bash
cd blog-agent
export OPENAI_API_KEY=your-key-here   # eger tanimli degilse
```

---

## Katman 1 — Spring Boot + Embabel Shell

### Test 1.1: Temiz Build

```bash
./mvnw clean compile
```

**Beklenen:**
- `BUILD SUCCESS` mesaji
- 6 source file derlenmis olmali

### Test 1.2: Uygulama Baslatma

```bash
./mvnw spring-boot:run
```

**Beklenen:**
- Embabel ASCII art gorunur
- Shell prompt acilir (interaktif komut satiri)

### Test 1.3: Shell Komutlari

Shell icinde sirayla calistir:

```
help
```
**Beklenen:** Kullanilabilir komutlarin listesi gorunur.

```
models
```
**Beklenen:** `gpt-5-mini` ve `gpt-5` listelenir.

```
agents
```
**Beklenen:** "Write and review a blog post about a given topic" gorunur.

```
goals
```
**Beklenen:** "A reviewed and polished blog post" gorunur.

```
actions
```
**Beklenen:** Su iki action listelenir:
1. "Write a first draft of the blog post"
2. "Review and improve the draft"

Shell'den cikmadan Katman 2 testlerine devam edin.

---

## Katman 2 — Draft Action (writeDraft)

### Test 2.1: Blog Taslagi Uretme

Shell icinde:

```
x Spring Boot nedir
```

**Beklenen:**
- Agent calismaya baslar
- `writeDraft` action calisir (gpt-5-mini kullanir)
- Console'da `BlogDraft` donusu gorunur (title + content)
- Ardindan `reviewDraft` action calisir (cunku @AchievesGoal reviewDraft'ta)

**Not:** Eger "no suitable model found" hatasi alirsan, `application.yaml`'daki indentation'i kontrol et.

---

## Katman 3 — Review Action + GOAP Pipeline

### Test 3.1: Iki Asamali Pipeline

Shell icinde:

```
x Virtual threads nedir
```

**Beklenen:**
1. Once `writeDraft` calisir (gpt-5-mini) → `BlogDraft` uretir
2. Sonra `reviewDraft` calisir (gpt-5) → `ReviewedPost` uretir
3. Console'da title, content ve feedback gorunur
4. Token kullanim istatistikleri (token sayisi, maliyet) gorunur

### Test 3.2: Verbose Mod ile Pipeline Dogrulamasi

```
x -p Microservices nedir
```

**Beklenen:**
- LLM'e gonderilen prompt'lar console'da gorunur
- Draft prompt'u ve review prompt'u ayri ayri gorulebilir
- Hangi model'in hangi action icin kullanildigini dogrulayabilirsin

---

## Katman 4 — Dosya Yazma + Personas

### Test 4.1: Blog Yazisi Dosyaya Kaydediliyor mu?

Shell icinde:

```
x Claude Code nedir
```

**Beklenen:**
- Draft ve review pipeline calisiyor
- Console'da "Blog post written to .../blog-posts/claude-code-nedir.md" mesaji gorunur

Shell'den cik (`exit`) ve dosyayi dogrula:

```bash
ls blog-posts/
cat blog-posts/*.md
```

**Beklenen:**
- `blog-posts/` klasorunde `.md` dosyasi mevcut
- Dosya icerigi Markdown formatinda, baslik ve icerik olmali

### Test 4.2: Persona Etkisini Dogrulama

Uygulamayi tekrar baslat:

```bash
./mvnw spring-boot:run
```

Shell icinde verbose mod ile calistir:

```
x -p Coding agent'lar nasil calisir
```

**Beklenen (WRITER persona):**
- Draft prompt'unda su bilgiler gorunur:
  - Role: "Software Developer and Educator"
  - Goal: "Write practical, beginner-friendly blog posts"
  - Backstory: "Experienced developer who loves teaching through clear, simple writing"

**Beklenen (REVIEWER persona):**
- Review prompt'unda su bilgiler gorunur:
  - Role: "Technical Editor"
  - Goal: "Review and polish technical blog posts"
  - Backstory: "Seasoned editor focused on clarity, accuracy, and tight writing"

**Beklenen (dosya):**
- `blog-posts/` klasorunde yeni bir `.md` dosyasi olusur

---

## Test 5: Dosya Isimlendirme Kontrolu

### Test 5.1: Ozel Karakterli Baslik

Shell icinde:

```
x C++ ile memory management: smart pointer'lar
```

Shell'den cik ve dosya adini kontrol et:

```bash
ls blog-posts/
```

**Beklenen:**
- Dosya adi kucuk harflerle, ozel karakterler tire ile degistirilmis olmali
- Ornek: `c-ile-memory-management-smart-pointer-lar.md`
- Dosya uzantisi `.md` olmali

---

## Temizlik

Testler bittikten sonra uretilen blog yazilarini temizleyebilirsin:

```bash
rm -rf blog-posts/
```

**Not:** `blog-posts/` dizini `.gitignore`'da tanimli, bu yuzden repo'ya eklenmez.

---

## Hizli Kontrol Listesi

| # | Test | Sonuc |
|---|------|-------|
| 1.1 | Temiz build basarili | [ ] |
| 1.2 | Uygulama basliyor | [ ] |
| 1.3 | Shell komutlari calisiyor | [ ] |
| 2.1 | Draft action calisiyor | [ ] |
| 3.1 | Iki asamali pipeline calisiyor | [ ] |
| 3.2 | Verbose mod prompt'lari gosteriyor | [ ] |
| 4.1 | Blog yazisi dosyaya kaydediliyor | [ ] |
| 4.2 | Persona bilgileri prompt'ta gorunuyor | [ ] |
| 5.1 | Ozel karakterli baslik dogru dosya adi uretiyor | [ ] |
