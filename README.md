<div align="center">

# 💎 TwinsShards

**Gelişmiş, Yüksek Performanslı ve Folia Destekli Kristal/Shard Ekonomi Eklentisi**  
*Advanced, High-Performance & Folia-Supported Crystal/Shard Economy Plugin*

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-62B47A?logo=minecraft&logoColor=white)](https://papermc.io/)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Folia-blue)](https://papermc.io/software/folia)
[![Database](https://img.shields.io/badge/Database-SQLite%20%7C%20MySQL%20(HikariCP)-00758F?logo=mysql&logoColor=white)](https://github.com/brettwooldridge/HikariCP)
[![PlaceholderAPI](https://img.shields.io/badge/PlaceholderAPI-Supported-brightgreen)](https://www.spigotmc.org/resources/placeholderapi.6245/)

[🇹🇷 Türkçe](#-türkçe) • [🇬🇧 English](#-english)

---

</div>

## 🇹🇷 Türkçe

**TwinsShards**, Minecraft sunucunuz için özel olarak tasarlanmış modern bir ikincil ekonomi (kristal/shard) eklentisidir. **Folia** ve modern Paper sürümleriyle (1.20+) tam uyumlu olup, yüksek sunucu yüklerinde bile asenkron veritabanı yapısı ve bellek içi önbellekleme (in-memory cache) sayesinde sıfır lag garantisi sunar.

### ✨ Özellikler
- 🚀 **Folia & Paper 1.20+ Desteği:** Folia'nın parçalı iş parçacığı (regionized multithreading) mimarisiyle %100 uyumludur.
- 🗄️ **Çift Veritabanı Desteği:** İster hafif yerel `SQLite`, ister yüksek ölçekli ağlar için bağlantı havuzlu `MySQL (HikariCP)`.
- ⚡ **Önbellek & Asenkron Kayıt:** Oyuncu bakiyeleri RAM üzerinde tutulur ve belirli aralıklarla arka planda veritabanına aktarılır.
- 🌐 **Çoklu Dil Desteği:** Türkçe (`messages_tr.yml`) ve İngilizce (`messages_en.yml`) dil dosyaları hazır gelir.
- 🏆 **Asenkron Sıralama (Top 10):** Sunucu ana iş parçacığını yormadan periyodik güncellenen liderlik tablosu.
- 🔢 **Sayı Kısaltmaları:** Büyük sayılar için dinamik ve ayarlanabilir biçimlendirme (`1.5K`, `2.3M`, `4.8B` vb.).
- 🔌 **PlaceholderAPI Entegrasyonu:** Skor tabloları, tab listeleri ve hologramlar için zengin placeholder desteği.
- 🎨 **Modern Renk & Biçimlendirme:** MiniMessage (`<gradient>`, `<b>`, vb.), Hex (`&#RRGGBB`) ve klasik (`&a`) renk desteği ile modern prefix/mesajlar.
- 🛠️ **Geliştirici API'si:** Diğer eklentilerin kristal bakiyelerine doğrudan erişip işlem yapabileceği `ShardsAPI`.

---

### 💻 Komutlar & Yetkiler

Ana komut: `/kristal` *(Alternatifler: `/crystal`, `/cr`, `/shards`, `/shard`, `/ts`, `/twinsshards`)*

| Komut | Açıklama | Yetki |
| :--- | :--- | :--- |
| `/kristal` | Kendi kristal bakiyenizi görüntüler | `twinsshards.use` |
| `/kristal bak [oyuncu]` | Başka bir oyuncunun bakiyesini görüntüler | `twinsshards.use` |
| `/kristal gonder <oyuncu> <miktar>` | Bir oyuncuya kristal transfer eder | `twinsshards.pay` |
| `/kristal top` | En yüksek kristale sahip oyuncuları listeler | `twinsshards.top` |
| `/kristal ekle <oyuncu> <miktar>` | Belirtilen oyuncuya kristal ekler | `twinsshards.admin.give` |
| `/kristal sil <oyuncu> <miktar>` | Belirtilen oyuncudan kristal eksiltir | `twinsshards.admin.take` |
| `/kristal ayarla <oyuncu> <miktar>` | Belirtilen oyuncunun kristalini sabitler | `twinsshards.admin.set` |
| `/kristal sifirla <oyuncu>` | Belirtilen oyuncunun kristalini sıfırlar | `twinsshards.admin.reset` |
| `/kristal yenile` | Yapılandırma ve dil dosyalarını yeniden yükler | `twinsshards.admin.reload` |


---

### 🧩 PlaceholderAPI Değişkenleri

| Placeholder | Açıklama | Örnek Çıktı |
| :--- | :--- | :--- |
| `%twinsshards_balance%` | Ham bakiye sayısı | `1500000` |
| `%twinsshards_balance_formatted%` | Kısaltılmış bakiye | `1.50M` |
| `%twinsshards_balance_commas%` | Virgülle ayrılmış bakiye | `1,500,000` |
| `%twinsshards_top_<sıra>%` | Sıralama satırı (config formatlı) | `1. Steve » 1.50M Shards` |
| `%twinsshards_top_name_<sıra>%` | Sıradaki oyuncunun adı | `Steve` |
| `%twinsshards_top_balance_<sıra>%` | Sıradaki oyuncunun ham bakiyesi | `1500000` |
| `%twinsshards_top_balance_formatted_<sıra>%` | Sıradaki oyuncunun kısaltılmış bakiyesi | `1.50M` |

---

### ☕ Geliştirici API'si (Developer API)

Eklentinize `TwinsShards` entegrasyonu yapmak için:

```java
import com.twinsshards.api.ShardsAPI;

// Bakiye sorgulama
double balance = ShardsAPI.getBalance(player);

// Bakiye kontrolü
if (ShardsAPI.hasBalance(player, 100.0)) {
    // Kristal düşme
    ShardsAPI.takeBalance(player, 100.0);
}

// Kristal verme
ShardsAPI.giveBalance(player, 250.0);
```

---

<div align="center">
  <br />
  <h2>🇬🇧 English</h2>
</div>

**TwinsShards** is an advanced secondary economy (crystal/shard) plugin crafted for modern Minecraft servers. Built with native **Folia** and Paper 1.20+ compatibility, it guarantees lag-free performance via in-memory caching and non-blocking asynchronous database operations.

### ✨ Key Features
- 🚀 **Native Folia & Paper 1.20+ Support:** Tailored for Folia's threaded regions and Paper's modern scheduler.
- 🗄️ **Dual Database Engine:** Out-of-the-box local `SQLite` or enterprise-grade `MySQL (HikariCP)` connection pooling.
- ⚡ **Asynchronous & In-Memory:** Player balances reside in RAM and flush asynchronously to disk/database.
- 🌐 **Multi-Language Ready:** Fully translatable with native English (`messages_en.yml`) and Turkish (`messages_tr.yml`).
- 🏆 **Async Leaderboards (Top 10):** Cached leaderboards that update without causing TPS drops.
- 🔢 **Custom Number Suffixes:** Configurable suffix formatting (`K`, `M`, `B`, `T`, etc.).
- 🔌 **PlaceholderAPI Hook:** Seamless integration with scoreboards, tab lists, menus, and holograms.
- 🎨 **Modern Colors & Formatting:** Full support for MiniMessage (`<gradient>`, `<b>`, etc.), Hex (`&#RRGGBB`), and legacy (`&a`) color codes.
- 🛠️ **Lightweight Developer API:** Simple static `ShardsAPI` for easy plugin hooking.

---

### 💻 Commands & Permissions

Primary command: `/shards` *(Aliases: `/shard`, `/kristal`, `/crystal`, `/cr`, `/ts`, `/twinsshards`)*

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/shards` | View your own shard balance | `twinsshards.use` |
| `/shards view [player]` | View another player's balance | `twinsshards.use` |
| `/shards pay <player> <amount>` | Transfer shards to a player | `twinsshards.pay` |
| `/shards top` | View shard leaderboard | `twinsshards.top` |
| `/shards give <player> <amount>` | Add shards to a player | `twinsshards.admin.give` |
| `/shards take <player> <amount>` | Remove shards from a player | `twinsshards.admin.take` |
| `/shards set <player> <amount>` | Set a player's shard balance | `twinsshards.admin.set` |
| `/shards reset <player>` | Reset a player's balance to default | `twinsshards.admin.reset` |
| `/shards reload` | Reload configuration and language files | `twinsshards.admin.reload` |

---

### 🧩 PlaceholderAPI Placeholders

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%twinsshards_balance%` | Raw numeric balance | `1500000` |
| `%twinsshards_balance_formatted%` | Compact formatted balance | `1.50M` |
| `%twinsshards_balance_commas%` | Comma-separated balance | `1,500,000` |
| `%twinsshards_top_<rank>%` | Full formatted leaderboard line | `1. Steve » 1.50M Shards` |
| `%twinsshards_top_name_<rank>%` | Leaderboard player name at rank | `Steve` |
| `%twinsshards_top_balance_<rank>%` | Leaderboard raw balance at rank | `1500000` |
| `%twinsshards_top_balance_formatted_<rank>%` | Leaderboard compact balance | `1.50M` |

---

### ☕ Developer API

Easily interact with TwinsShards from your own plugin:

```java
import com.twinsshards.api.ShardsAPI;

// Check balance
double balance = ShardsAPI.getBalance(player);

// Check if player has enough shards
if (ShardsAPI.hasBalance(player, 500.0)) {
    // Deduct balance
    ShardsAPI.takeBalance(player, 500.0);
}

// Deposit shards
ShardsAPI.giveBalance(player, 1000.0);
```

---

<div align="center">
Developed with ❤️ for the Minecraft Community.
</div>
