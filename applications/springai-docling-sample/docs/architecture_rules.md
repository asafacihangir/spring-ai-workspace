# Architecture Rules: Contract-Based Modular Monolith

Bu doküman, projenin mimari standartlarını belirler. Proje, iş alanlarına (Business Domains) dayalı Feature-Based bir yapı izler. Modüller arası iletişim, katı bir Contracts (Provider/Consumer) deseni ile yönetilir.

---

## 1. Core Principles

### Feature-First Organization

- Proje yapısı teknik katmanları değil, iş yeteneklerini yansıtır.
- Her özellik (örn: employee, hierarchy, leave-request) kendi kendine yeten bir modüldür.

### Strict Module Boundaries

- Modüller birbirinin domain, repository veya entity sınıflarına **ASLA** doğrudan erişemez.
- Tüm iletişim `contracts` paketi üzerinden, Provider (Sağlayıcı) ve Consumer (Tüketici) rolleriyle yönetilir.

---

## 2. Package Structure Template

Her Feature paketi aşağıdaki standart yapıya sahiptir:

```
com.app.feature_name
├── contracts/                       # MODULE INTERFACE LAYER
│   ├── outbound/                    # [PROVIDER] Dışarıya sunduğum hizmetler (API)
│   │   ├── FeatureApi.java          # Public Facade Implementation
│   │   └── model/                   # Contract DTOs
│   │       └── FeatureContractDTO.java
│   │
│   └── inbound/                     # [CONSUMER] Dışarıdan aldığım hizmetler (Ports)
│       ├── OtherFeaturePort.java        # Interface (Benim ihtiyacım)
│       └── OtherFeaturePortAdapter.java # Adapter (Diğer modülün API'sine bağlanan)
│
├── domain/                          # BUSINESS LOGIC (Core)
│   ├── Feature.java                 # Entity
│   ├── FeatureService.java          # Business Logic
│   └── FeatureRepository.java       # DB Interface
│
├── infrastructure/                  # PERSISTENCE (Driven Adapters)
│   ├── JpaFeatureRepository.java
│   └── FeatureEntity.java           # DB Entity
│
└── api/                             # REST API (External Clients / Frontend Only)
    └── FeatureController.java
```

---

## 3. Communication Pattern: Provider vs Consumer

İletişim, veriyi Sunan (Provider) ve veriyi Kullanan (Consumer) modüller arasında tanımlı kurallarla gerçekleşir.

### 3.1. Provider Module (Veriyi Sunan)

- **Konum:** `contracts/outbound`
- Diğer modüllerin kullanması için `XxxApi` sınıfını sunar.
- Kendi içindeki Service/Domain mantığını `...ContractDTO` nesnelerine dönüştürerek dışarı açar.

> **Örnek:** Employee modülü, personel bilgisini `EmployeeApi` üzerinden sunar.

### 3.2. Consumer Module (Veriyi Kullanan)

- **Konum:** `contracts/inbound`
- Başka bir modüle ihtiyacı olduğunda, o modülü doğrudan çağırmaz.
- Kendi ihtiyacını tanımlayan bir **Port** (Interface) yazar (`XxxPort`).
- Bu portu, diğer modülün API'sine bağlayan bir **Adapter** yazar (`XxxPortAdapter`).

---

## 4. Implementation Rules

### 4.1. Naming Conventions

| Rol | Sınıf Tipi | İsimlendirme Kuralı | Konum |
|-----|------------|---------------------|-------|
| Provider | Implementation | `FeatureApi` | `contracts/outbound` |
| Provider | DTO | `FeatureNameContractDTO` | `contracts/outbound/model` |
| Consumer | Interface | `TargetFeaturePort` | `contracts/inbound` |
| Consumer | Adapter | `TargetFeaturePortAdapter` | `contracts/inbound` |

### 4.2. Data Flow Example

**Senaryo:** LeaveRequest modülü (Consumer), Employee modülünden (Provider) personel bilgisi istiyor.

**Employee Module (Provider):**
- `employee/contracts/outbound/EmployeeApi.java` - Servisi dışarı açar
- `employee/contracts/outbound/model/EmployeeContractDTO.java` - Veri modeli

**LeaveRequest Module (Consumer):**
- `leaverequest/contracts/inbound/EmployeePort.java` - Interface tanımlar: "Bana bu veri lazım"
- `leaverequest/contracts/inbound/EmployeePortAdapter.java` - EmployeeApi'yi inject eder ve Port'u implemente eder

**Kullanım (Service Layer):**

```java
@Service
public class LeaveRequestService {
    private final EmployeePort employeePort; // Sadece Port'u bilir, API'yi bilmez.

    public void createRequest(String empId) {
        EmployeeContractDTO emp = employeePort.getEmployee(empId);
    }
}
```

---

## 5. Benefits of This Structure

| Fayda | Açıklama |
|-------|----------|
| **Loose Coupling** | Consumer modül (LeaveRequest), Provider modülün (Employee) sadece API'sine bağımlıdır, iç yapısını bilmez. `EmployeePort` sayesinde bağımlılık tersine çevrilir. |
| **Explicit Boundaries** | `contracts/inbound` klasörüne bakıldığında, modülün dış dünyadan nelere bağımlı olduğu tek bakışta görülür. `contracts/outbound` klasörüne bakıldığında, modülün dış dünyaya neler sunduğu görülür. |
| **Refactoring Safety** | Internal DTO'lar ile Contract DTO'lar ayrıldığı için, veritabanı şeması değişse bile kontratlar bozulmaz. |

---

## 6. Anti-Patterns (Yapılmaması Gerekenler)

| Anti-Pattern | Açıklama |
|--------------|----------|
| **Direct Service Injection** | `LeaveRequestService` içine `EmployeeService` inject etmek yasaktır. |
| **Sharing Entities** | Contract metotlarından Hibernate Entity dönmek yasaktır. Sadece `ContractDTO` dönülebilir. |
| **Circular Dependency** | `contracts` katmanında karşılıklı bağımlılık yaratılmamalıdır. |

---

*Son güncelleme: 2024-12-25*
