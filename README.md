# Sisihin

<img src="playstore_icon_512.png" width="96" alt="Sisihin">

Aplikasi Android untuk mencatat celengan dan target tabungan.

Saya bikin ini karena susah cari aplikasi nabung yang simpel. Kebanyakan minta daftar akun, ada iklan, atau fiturnya kebanyakan sampai bingung sendiri. Sisihin nggak begitu: buka aplikasi, bikin celengan, catat setoran, selesai. Semua data disimpan di HP sendiri, nggak dikirim ke server mana pun.

Namanya dari kata "sisihin" — sisihkan uang sedikit-sedikit sampai targetnya kebeli.

[![GitHub release](https://img.shields.io/github/v/release/mhafizhibrahim/sisihin)](https://github.com/mhafizhibrahim/sisihin/releases/latest)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-brightgreen)

Versi terbaru: **[v1.1.2](https://github.com/mhafizhibrahim/sisihin/releases/tag/v1.1.2)** (GitHub Releases)

## Fitur

- Bikin celengan dengan target nominal, foto, dan rencana nabung harian/mingguan/bulanan. Ada estimasi kapan target tercapai.
- Setoran cepat lewat chip nominal (nominal yang paling sering kamu pakai muncul otomatis). Kalau salah pencet bisa di-urungkan.
- Penarikan juga dicatat, jadi saldonya selalu sesuai uang yang beneran ada.
- Celengan yang sudah 100% pindah sendiri ke tab Tercapai. Arsipnya read-only biar nggak keubah nggak sengaja.
- Pengingat nabung lewat notifikasi, tetap jalan setelah HP restart.
- Kunci aplikasi pakai sidik jari/PIN. Bisa atur jeda kunci otomatis (segera, 30 detik, 1 menit).
- Tema gelap/terang ala Instagram, atau ikut sistem.
- 7 mata uang: IDR, USD, JPY, EUR, SGD, MYR, GBP.
- Kalkulator buat ngitung perlu nyisihin berapa per hari supaya target kekejar.
- Backup dan restore ke file JSON. Pindah HP tinggal bawa filenya.
- Cek update aplikasi dari GitHub Releases (jelasan di bawah).

## Download

Ambil APK terbaru di halaman [Releases](https://github.com/mhafizhibrahim/sisihin/releases/latest), buka filenya di HP, izinkan install dari sumber tidak dikenal kalau diminta. Minimal Android 8.0.

## Soal update aplikasi

Karena aplikasinya nggak lewat Play Store, update dicek langsung ke GitHub Releases. Waktu aplikasi dibuka, dia ngecek sekali di background apakah ada rilis yang lebih baru. Kalau ada, muncul dialog berisi changelog dan tombol download APK-nya. Kalau lagi offline atau GitHub-nya nggak bisa diakses, ya udah, diam aja — nggak ada error yang muncul.

Bisa juga dicek manual lewat Pengaturan > Cek Pembaruan.

Ngeceknya pakai `HttpURLConnection` biasa, nggak pakai Retrofit/OkHttp, biar APK tetap kecil.

## Build sendiri

Butuh Android Studio atau minimal JDK 17 + Android SDK 35.

```
git clone https://github.com/mhafizhibrahim/sisihin.git
cd sisihin
gradlew.bat installDebug
```

Build debug langsung jalan tanpa konfigurasi tambahan.

Untuk build release, taruh `keystore.properties` di root repo:

```properties
storePassword=...
keyPassword=...
```

File ini (dan file `.jks`) sudah masuk `.gitignore`, jadi nggak akan ke-commit. Lalu jalankan `gradlew.bat assembleRelease`.

Unit test: `gradlew.bat testDebugUnitTest`.

## Teknis

Kotlin + Jetpack Compose (Material 3), Room untuk database lokal, DataStore untuk preferensi, Coil untuk foto celengan. Navigasi antar tab bisa digeser. Nggak ada dependency jaringan sama sekali selain API bawaan Android.

Kode dibagi per layer di `app/src/main/java/com/sisihin/app/`: `core` (tema, komponen, util), `data` (Room, repository, update checker), `presentation` (screen Compose), `notification` (pengingat).

## Catatan

Kalau suatu saat rilis di Play Store, mekanisme download APK dari GitHub ini harus diganti ke Play In-App Updates, soalnya Play nggak mengizinkan aplikasi mengunduh APK dari luar toko. Untuk distribusi langsung seperti sekarang, aman.

Ada bug atau saran? Buka [issue](https://github.com/mhafizhibrahim/sisihin/issues) saja.
