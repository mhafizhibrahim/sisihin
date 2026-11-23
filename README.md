<div align="center">

<img src="playstore_icon_512.png" width="140" alt="Logo Sisihin"/>

# SISIHIN

**Celengan digital rasa Instagram — nabung jadi seru, target jadi nyata.** 🐷✨

[![Rilis Terbaru](https://img.shields.io/github/v/release/mhafizhibrahim/sisihin?style=for-the-badge&label=RILIS&color=0095F6&logo=github)](https://github.com/mhafizhibrahim/sisihin/releases/latest)
[![Unduhan](https://img.shields.io/github/downloads/mhafizhibrahim/sisihin/total?style=for-the-badge&label=UNDUHAN&color=2ECC71&logo=android)](https://github.com/mhafizhibrahim/sisihin/releases)
[![Stars](https://img.shields.io/github/stars/mhafizhibrahim/sisihin?style=for-the-badge&color=ED4956&logo=github)](https://github.com/mhafizhibrahim/sisihin/stargazers)

[![Android](https://img.shields.io/badge/Android_8.0+-3DDC84?style=flat-square&logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin_2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](#)
[![Material 3](https://img.shields.io/badge/Material_3-0095F6?style=flat-square&logo=materialdesign&logoColor=white)](#)
[![100% Offline](https://img.shields.io/badge/100%25_Offline-000000?style=flat-square&logo=wifi&logoColor=white)](#)

[📲 Unduh APK](#-unduh--pasang) • [✨ Fitur](#-fitur-unggulan) • [🛠️ Teknologi](#️-teknologi) • [👨‍💻 Build Sendiri](#-build-dari-sumber)

</div>

---

## 🎯 Apa itu Sisihin?

**Sisihin** (dari kata *"sisihin"* — sisihkan!) adalah aplikasi pelacak **celengan & target tabungan** untuk Android. Punya impian beli gadget, liburan, atau dana darurat? Buat celengannya, sisihkan uang sedikit demi sedikit, dan lihat progresnya tumbuh — seperti melihat feed Instagram, tapi isinya masa depan kamu. 💪

> 💡 **Filosofi:** tanpa akun, tanpa iklan, tanpa ribet. Semua data tersimpan **100% di HP kamu**. Internet hanya dipakai untuk satu hal: mengecek apakah ada versi baru aplikasi.

<div align="center">

| 🖤 | 🤍 |
|:---:|:---:|
| Desain gelap ala Instagram | Desain terang yang bersih |
| Gelap • Terang • Ikuti Sistem | Ganti tema seketika, tanpa jeda |

</div>

---

## ✨ Fitur Unggulan

<table>
<tr>
<td width="50%">

### 🎯 Celengan Bertarget
Buat target tabungan dengan nama, foto impian, nominal target, dan rencana nabung **harian / mingguan / bulanan**. Estimasi kapan target tercapai dihitung otomatis.

</td>
<td width="50%">

### 💸 Nabung Sekali Ketuk
Chip nominal pintar: ketuk sekali → setoran langsung tercatat. Salah pencet? Ada tombol **Urungkan** di snackbar. Nominal bebas juga bisa lewat dialog catatan.

</td>
</tr>
<tr>
<td width="50%">

### 📊 Progres Real-time
Ringkasan total uang terkumpul, progres per celengan, dan riwayat setoran/penarikan harian. Urutkan berdasarkan target, persentase, tanggal, estimasi, atau nama.

</td>
<td width="50%">

### 🏆 Arsip Tercapai
Target tembus 100%? Celengan otomatis pindah ke tab **Tercapai** dengan banner perayaan dan lama pencapaian — arsip read-only yang aman dari edit tak sengaja.

</td>
</tr>
<tr>
<td width="50%">

### ⏰ Pengingat Nabung
Notifikasi pengingat terjadwal supaya konsisten nyisihin uang. Tetap jalan bahkan setelah HP di-restart.

</td>
<td width="50%">

### 🔒 Kunci Biometrik
Amankan data tabungan dengan **sidik jari / PIN layar**. Kunci otomatis saat aplikasi ditinggal, dengan jeda yang bisa diatur: Segera, 30 detik, atau 1 menit.

</td>
</tr>
<tr>
<td width="50%">

### 🌍 Multi Mata Uang
7 mata uang dengan format ribuan rapi: 🇮🇩 IDR • 🇺🇸 USD • 🇯🇵 JPY • 🇪🇺 EUR • 🇸🇬 SGD • 🇲🇾 MYR • 🇬🇧 GBP

</td>
<td width="50%">

### 🧮 Kalkulator Target
Bingung nyisihin berapa per hari? Kalkulator bawaan menghitung nominal setoran yang dibutuhkan agar target tercapai tepat waktu.

</td>
</tr>
<tr>
<td width="50%">

### 💾 Cadangkan & Pulihkan
Ekspor seluruh celengan + riwayat ke file **JSON**, pulihkan kapan saja di HP baru. Data kamu, kendali kamu.

</td>
<td width="50%">

### 🔄 Update In-App
Cek pembaruan otomatis via **GitHub Releases** — pop-up ala Instagram muncul saat ada versi baru, lengkap dengan changelog dan tombol unduh. Bisa juga dicek manual dari Pengaturan.

</td>
</tr>
</table>

---

## 📲 Unduh & Pasang

1. Buka halaman **[Rilis Terbaru](https://github.com/mhafizhibrahim/sisihin/releases/latest)**
2. Unduh file `sisihin-vX.Y.Z.apk`
3. Buka file-nya di HP → izinkan *"Install dari sumber tidak dikenal"* bila diminta
4. Selesai! Mulai sisihkan uangmu 🐷

> 📱 Minimal **Android 8.0 (Oreo)** • APK ringan ±13 MB • Ditandatangani keystore resmi rilis

---

## 🔄 Cara Kerja Update In-App

```
Buka aplikasi ──► GET api.github.com/repos/.../releases/latest
                        │
              tag_name > versionName?
              ┌─────────┴─────────┐
             Ya                 Tidak / offline / gagal
              │                     │
   Pop-up "Pembaruan Tersedia"   Diam saja 🤫
   [Update Sekarang] [Nanti Saja]  (tanpa error)
```

- ⚡ Tanpa library tambahan — murni `HttpURLConnection` + `org.json`, timeout 5 detik
- 🤫 **Fail-silent**: offline, rate-limit, atau belum ada rilis? Tidak ada gangguan sama sekali
- 🔢 Perbandingan *semantic versioning* akurat: `1.0.10` > `1.0.2`

---

## 🛠️ Teknologi

| Layer | Teknologi |
|---|---|
| Bahasa | **Kotlin 2.0** + Coroutines & Flow |
| UI | **Jetpack Compose** (Material 3) — desain kustom ala Instagram, tanpa dynamic color |
| Database | **Room** (SQLite) — 100% lokal |
| Preferensi | **DataStore Preferences** |
| Navigasi | Navigation Compose + HorizontalPager (geser antar tab 👆) |
| Gambar | **Coil** — foto celengan dari galeri |
| Keamanan | AndroidX **Biometric** |
| Update | GitHub Releases API (tanpa dependency jaringan!) |
| Build | AGP 8.7 • KSP • Version Catalog |

## 🏗️ Arsitektur

```
app/src/main/java/com/sisihin/app/
├── core/            # Tema (token warna Instagram), komponen, util, keamanan
├── data/
│   ├── local/       # Room: entity, DAO, database
│   ├── preferences/ # DataStore
│   ├── repository/  # SavingsRepository + BackupRestoreManager
│   └── update/      # UpdateChecker (GitHub Releases)
├── domain/model/    # Enum & model bisnis
├── notification/    # Pengingat nabung + boot receiver
└── presentation/    # Compose screens: home, detail, form, settings, lock, update
```

---

## 👨‍💻 Build dari Sumber

```bash
git clone https://github.com/mhafizhibrahim/sisihin.git
cd sisihin
```

Buat file `keystore.properties` di root (opsional — hanya untuk build release):

```properties
storePassword=password_keystore_kamu
keyPassword=password_key_kamu
```

Lalu:

```bash
.\gradlew.bat testDebugUnitTest   # ✅ unit test
.\gradlew.bat installDebug        # 📱 pasang ke HP
.\gradlew.bat assembleRelease     # 📦 APK rilis (butuh keystore)
```

> 🔐 File `keystore.properties`, `*.jks`, dan `local.properties` **tidak pernah** masuk ke repo (lihat `.gitignore`).

## 🚀 Alur Rilis

1. Naikkan `versionCode` & `versionName` di `app/build.gradle.kts`
2. `.\gradlew.bat assembleRelease`
3. GitHub → **Releases** → *Draft a new release* → tag `vX.Y.Z` + changelog
4. Lampirkan `app-release.apk` → **Publish**
5. 🎉 Semua pengguna versi lama langsung melihat pop-up update

---

<div align="center">

**Dibuat dengan ❤️ dan Jetpack Compose**

*Konsisten nyisihin hari ini, nikmati hasilnya nanti.* 🐷💰

⭐ Suka Sisihin? Kasih bintang di repo ini!

</div>
