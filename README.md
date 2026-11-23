<img src="playstore_icon_512.png" width="80" alt="Ikon Sisihin">

# Sisihin

Pencatat celengan dan target tabungan yang jalan offline di Android.

[Website & simulasi interaktif](https://sisihin-web.vercel.app/) — bisa coba langsung di browser, unduh APK terbaru juga di sana · [Download APK (Releases)](https://github.com/mhafizhibrahim/sisihin/releases/latest) · [Laporkan bug](https://github.com/mhafizhibrahim/sisihin/issues)

[![Website](https://img.shields.io/badge/Website-Demo%20%26%20APK-303030?style=flat)](https://sisihin-web.vercel.app/)
[![GitHub Release](https://img.shields.io/github/v/release/mhafizhibrahim/sisihin?style=flat&label=Release&color=303030)](https://github.com/mhafizhibrahim/sisihin/releases/latest)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-303030?style=flat)

---

## Kenapa dibikin

Susah cari aplikasi nabung yang simpel. Kebanyakan minta daftar akun, ada iklan, atau fiturnya kebanyakan sampai bingung sendiri. Sisihin nggak begitu: buka aplikasi, bikin celengan, catat setoran, selesai. Semua data cukup tersimpan di HP sendiri, nggak dikirim ke server mana pun.

Namanya dari kata "sisihin" — sisihkan uang sedikit-sedikit sampai targetnya kebeli.

## Fitur utama

- Bikin celengan dengan target nominal, foto barang, dan rencana nabung harian/mingguan/bulanan. Ada estimasi tanggal target tercapai.
- Catat `+ Tambah` / `- Kurangi`. Chip nominal setoran cepat muncul otomatis dari riwayat, dan salah pencet bisa di-urungkan lewat snackbar.
- Celengan yang sudah 100% pindah sendiri ke tab **Tercapai** dan jadi *read-only* biar nggak keubah nggak sengaja.
- **Kalkulator Target** dengan dua mode: **Cari Nominal** dan **Cari Waktu**.
- Pengingat nabung harian lewat notifikasi (tetap jalan setelah HP restart) dan kunci aplikasi pakai biometrik/PIN.
- Ganti tema Gelap/Terang seketika ala Instagram, atau ikut sistem.
- 10 mata uang: `IDR`, `USD`, `JPY`, `EUR`, `SGD`, `MYR`, `KRW`, `GBP`, `AUD`, `SAR`.
- Backup dan restore lokal ke file JSON, jadi pindah HP tinggal bawa filenya.
- Cek pembaruan otomatis lewat GitHub Releases.

## Coba dan download

Mampir ke **[sisihin-web.vercel.app](https://sisihin-web.vercel.app/)** kalau mau lihat fiturnya dulu, coba simulasi HP langsung di browser, atau unduh APK di sana. Tautan langsungnya ada di [GitHub Releases](https://github.com/mhafizhibrahim/sisihin/releases/latest) (versi terbaru saat ditulis: v1.1.3).

Pasang APK-nya dengan membuka file di HP, lalu izinkan instalasi dari sumber tidak dikenal kalau diminta. Minimal Android 8.0.

## Soal auto-update

Aplikasinya nggak lewat Play Store, jadi update dicek langsung ke GitHub Releases API. Waktu dibuka, aplikasi ngecek sekali di background: kalau ada rilis yang lebih baru, muncul dialog berisi catatan rilis dan tombol unduh APK. Versi dibandingkan per segmen angka, jadi `1.0.10` dianggap lebih baru dari `1.0.2`.

Ceknya pakai `HttpURLConnection` dan `org.json` bawaan Android, tanpa Retrofit/OkHttp, biar APK tetap kecil. Timeout-nya 5 detik, dan semua kegagalan (offline, 404, rate-limit, JSON rusak) dibiarkan senyap — nggak ada dialog error. Kalau pengen cek sekarang juga, lewat **Pengaturan > Cek Pembaruan**.

## Teknis dan build sendiri

| Bagian | Dipakai |
| --- | --- |
| Bahasa & UI | Kotlin, Jetpack Compose, Material 3 |
| Data lokal | Room, DataStore |
| Lainnya | Coil untuk foto, navigasi Compose, Biometric |
| Update | GitHub Releases API via `HttpURLConnection` + `org.json` |

Kode dibagi per layer di `app/src/main/java/com/sisihin/app/`:

| Folder | Isinya |
| --- | --- |
| `core` | tema, komponen bersama, util |
| `data` | Room, repository, update checker |
| `domain` | model dan enum |
| `notification` | pengingat nabung |
| `presentation` | layar Compose |

Tema mengikuti kontrak di `DESIGN_RULES.md`: token Instagram Dark/Light, divider 0.8.dp, dan bottom bar tiga tab (Tabungan, Tercapai, Pengaturan). Ganti tema langsung, tanpa transisi.

Butuh Android Studio, atau minimal JDK 17 dan Android SDK 35.

```bat
git clone https://github.com/mhafizhibrahim/sisihin.git
cd sisihin
gradlew.bat installDebug
```

Build debug jalan tanpa konfigurasi tambahan. Untuk build release, taruh `keystore.properties` di root repo:

```properties
storePassword=...
keyPassword=...
```

Bareng file `sisihin-release.jks`. Keduanya sudah masuk `.gitignore`, jadi nggak akan ke-commit. Lalu jalankan `gradlew.bat assembleRelease`.

Unit test: `gradlew.bat testDebugUnitTest`.

---

## Catatan penutup

Distribusinya masih langsung lewat APK GitHub, bukan Play Store. Kalau suatu saat rilis di Play Store, mekanisme unduh APK dari dalam aplikasi harus diganti ke Play In-App Updates. Untuk distribusi langsung seperti sekarang, aman.

Ada bug atau saran? Buka [issue](https://github.com/mhafizhibrahim/sisihin/issues) saja. Repo website-nya ada di [mhafizhibrahim/sisihin-web](https://github.com/mhafizhibrahim/sisihin-web).
