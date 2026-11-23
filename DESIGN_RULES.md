# DESIGN_RULES — Sisihin

Kontrak desain ini berasal dari spesifikasi pengguna untuk migrasi tema "Liquid Glass"
ke tema Instagram Dark/Light. Anggap sebagai token tetap: jangan menambah warna atau
efek baru tanpa mengubah kontrak ini lebih dulu.

## Anchor

- Instagram Dark / Light. Tanpa dynamic color, tanpa wallpaper-derived palette.
- Sumber token: `app/src/main/java/com/sisihin/app/core/theme/Color.kt`
- Pembungkus tema: `app/src/main/java/com/sisihin/app/core/theme/Theme.kt`
- Pembaca token di UI: `LocalSisihinColors.current` dan `MaterialTheme.colorScheme`.
- Kontrol berstatus aktif/nonaktif (`Switch`, `FilterChip`) dan teks dialog/dropdown
  wajib memakai token eksplisit. Warna default Material pernah membuat switch
  biometrik tak terlihat di tema Gelap, jadi jangan mengandalkan default.

## Token warna

| Token | Dark | Light |
| --- | --- | --- |
| background / topBarBackground | `#000000` | `#FFFFFF` |
| cardSurface | `#121212` | `#FAFAFA` |
| elevatedSurface | `#262626` | `#FFFFFF` |
| photoPlaceholder | `#1C1C1E` | `#EFEFEF` |
| hairlineBorder / divider | `#262626` (0.8.dp solid) | `#DBDBDB` (0.8.dp solid) |
| textPrimary | `#F5F5F5` | `#000000` |
| textSecondary | `#A8A8A8` | `#737373` |
| textMuted | `#737373` | `#999999` |
| accentBlue | `#0095F6` | `#0095F6` |
| accentSoftContainer | `#18293A` | `#E7F3FF` |
| accentSoftContent | `#67B8FF` | `#0095F6` |
| positiveGreen | `#2ECC71` | `#16A34A` |
| negativeRed | `#ED4956` | `#ED4956` |

## Aturan aktif

- Dilarang: gradient specular, ambient radial glow, surface kaca transparan, `emoji_*.png`,
  `CustomEmojiIcon`, dan warna hardcoded di dalam composable.
- Ikon: vektor Material (`material-icons-extended`), kecuali `CurrencyFlagIcon` (`flag_*.png`).
- Garis pemisah dan border kartu memakai 0.8.dp solid dari `hairlineBorder` / `divider`.
- System bar mengikuti `AppThemeMode` (SYSTEM / DARK / LIGHT). `enableEdgeToEdge`
  hanya dipanggil sekali saat startup; pergantian tema cukup flip flag
  `WindowCompat.getInsetsController(...).isAppearanceLightStatusBars/NavigationBars`.
  Memanggil `enableEdgeToEdge` tiap ganti tema memicu relayout window dan jank.
- Semua ikon-only `IconButton` wajib punya `contentDescription`.

## Animasi

- Ganti tema **wajib seketika**, tanpa animasi visual apa pun: `SisihinTheme`
  langsung memasang token baru (satu rekomposisi). Dilarang:
  - meng-animasikan token/role warna per frame (merombak seluruh pohon UI tiap frame),
  - `Crossfade` (mereset scroll/halaman/field),
  - **layer penutup full-screen** dengan alpha (pernah membuat seluruh UI — termasuk
    teks — tertutup layar polos hitam/putih ketika alpha tidak kembali ke 0).
- System bar ikon diganti seketika lewat flag `WindowCompat` (bukan `enableEdgeToEdge`).
- Pager, pill navbar, chip pilihan tema, dan indikator progres memakai spring
  `DampingRatioNoBouncy` + `StiffnessMediumLow` (pager tambah `visibilityThreshold`).
- FAB `Tambah Celengan` muncul/hilang dengan `AnimatedVisibility` (fade + scale 0.9).
- `LazyColumn` memakai key stabil + `contentType` agar recycling saat scroll mulus.

## Layout & interaksi (revisi besar)

- Navigasi utama: bottom bar kustom 3 tab — `Tabungan`, `Tercapai`, `Pengaturan`
  (bukan `NavigationBar` Material 3 yang 80.dp). Strukturnya: `Surface` dengan latar
  `background`, `HorizontalDivider` 0.6.dp `hairlineBorder`, `Row` setinggi **66.dp murni**,
  lalu `Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))` terpisah
  (jangan `navigationBarsPadding()` pada `Row`, karena itu memakan tinggi konten).
  Tiap item: pill aktif 52×28.dp `RoundedCornerShape(50)` berisi ikon 22.dp
  (`accentBlue` saat aktif, `textMuted` saat nonaktif), `Spacer` 4.dp, label 11.sp
  dengan `lineHeight = 14.sp` (Medium saat aktif). Ruang bersih atas pill ~10.dp.
  Tidak ada tab atas dan tidak ada menu overflow di Beranda. Tab juga bisa dipindah
  dengan geser kiri/kanan lewat `HorizontalPager`; tap bottom nav dan gesture memakai
  satu `PagerState` yang sama. Gesture hanya aktif di 3 tab, bukan di Detail/Form.
  Pager menyiapkan 1 halaman di sekitar halaman aktif (`beyondViewportPageCount = 1`)
  dan memakai spring tanpa bounce yang sama untuk gesture maupun tap bottom nav
  agar perpindahan tab tidak tersendat.
- `HomeScreen` adalah inner `Scaffold` di dalam outer `Scaffold` pemegang bottom bar,
  jadi wajib `contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)` agar inset
  tidak diterapkan dua kali. Tidak boleh ada `navigationBarsPadding()` di konten tab.
  `LazyColumn` memakai `contentPadding` `(16, 8, 16, 80).dp`.
- FAB `Tambah Celengan` (tinggi 48.dp) bukan slot `Scaffold.floatingActionButton`,
  melainkan di dalam `Box(Modifier.fillMaxSize())` konten tab, di-align `BottomEnd`
  dengan `padding(end = 16.dp, bottom = 16.dp)` — 16.dp di atas garis atas bottom bar.
- Beranda `Tabungan`: header ringkasan "Total Uang Terkumpul" dari celengan aktif
  + jumlah target berjalan, diikuti kartu horizontal ringkas: foto kotak bulat di kiri,
  judul/nominal/progres linear tipis di kanan. Grid 2 kolom tidak dipakai.
  Kartu Beranda **tidak** memuat chip nominal.
- Pilihan nominal ada di halaman Detail: 3 chip berupa pill (gaya chip lama Beranda).
  Chip pertama = `planAmount` yang diisi saat membuat tabungan; dua chip berikutnya
  adalah nominal `DEPOSIT` paling sering di celengan itu saja (tanpa duplikat, urut
  dari frekuensi terbanyak lalu nominal terbesar). Chip dihitung ulang dari riwayat.
  Ketuk chip → setoran langsung + snackbar dengan aksi `Urungkan`.
  Dialog `Catat Tabungan` tetap dipakai untuk nominal bebas.
- Semua field nominal (Form, Kalkulator Target, `Catat Tabungan`) memakai
  `TextFieldValue` + `CurrencyFormatter.formatInput(TextFieldValue)` / `inputValue`,
  bukan `String`. Cursor dipetakan ke batas digit yang sama setelah titik ribuan:
  mengetik `5000` menghasilkan `5.000` dengan cursor di ujung (`5000|`), bukan
  `5.00|0`. Label statis dan chip boleh tetap memakai `formatInput(String)`.
- Halaman Detail: area progres (hero) di atas, saldo, lalu dua tombol besar
  `+ Tabung` / `- Tarik`. FAB kecil dihapus. Celengan tercapai menampilkan banner
  `100% Tercapai`, bukan area aksi atau kartu pengingat.
- Arsip `Tercapai` bersifat read-only: edit disembunyikan, pengingat mati dan tidak
  bisa dinyalakan lagi, transaksi tidak bisa diubah/dihapus. Yang tersisa: indikator
  100%, lama pencapaian, riwayat setoran harian, dan `Hapus`. Penegakan aturan ini
  wajib ada di `GoalDetailViewModel` **dan** `SavingsRepository`.
- `Cadangkan`, `Pulihkan`, dan `Tentang` pindah ke `Pengaturan` (bukan overflow).

## Verifikasi

```
.\gradlew.bat testDebugUnitTest
.\gradlew.bat installDebug
adb -s 192.168.1.3:5555 shell am start -n com.sisihin.app/.MainActivity
adb -s 192.168.1.3:5555 logcat -d -s AndroidRuntime:E
grep -R "emoji_" app/src/main
```
