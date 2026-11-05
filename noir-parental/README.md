# NOIR X BOOST — Parental Control (Versi Legal)

Project ini terdiri dari 2 bagian:

1. `admin-panel/` — Panel kontrol untuk orang tua (web app, HTML/JS/Firebase). Bisa dibuka
   langsung di browser, atau nanti dibungkus jadi APK pakai WebView/Capacitor kalau mau.
2. `child-app-android/` — Skeleton project Android (Kotlin) untuk app anak, bertema
   "game booster". Perlu dibuka & dilengkapi di Android Studio.

## Yang DIHAPUS dari permintaan awal (tidak akan ditambahkan dalam bentuk apa pun)

- Akses kamera depan/belakang jarak jauh
- Overlay HTML fullscreen yang tidak bisa ditutup oleh anak
- Proteksi "tidak bisa dihapus sama sekali" (anti-uninstall total)
- Penyamaran nama/ikon aplikasi, atau logic "pura-pura crash" saat login

## Yang DIGANTI ke versi legal

| Fitur asli | Implementasi di project ini |
|---|---|
| Overlay HTML paksa | Pesan dari ortu tampil sebagai **notifikasi Android normal** |
| Ubah wallpaper otomatis | Wallpaper hanya terpasang setelah anak menekan **Setujui** |
| Anti-uninstall total | **Device Admin API** — uninstall butuh menonaktifkan admin dulu, bisa dikunci dengan password ortu di sisi admin panel (lihat catatan di `UninstallProtectionReceiver.kt`) |
| Kamera | Diganti **SOS button**, **check-in terjadwal**, dan **geofence alert** sebagai cara memantau keselamatan anak |

## Yang tetap sesuai arahan

- Lokasi real-time
- Senter ON/OFF
- Role owner (centralvex) & admin, hanya owner yang bisa menambah admin
- Tema panel: coklat-merah gelap, background hitam, layout grid kotak-kotak
- Tema app anak: "game booster" (tombol boost, mod menu/FPS overlay yang bisa di-toggle)

## Setup yang perlu kamu siapkan

### 1. Firebase
- Project Firebase kamu sudah ada (config sudah dipakai di `admin-panel/firebase-config.js`).
- Di Firebase Console:
  - Aktifkan **Authentication > Email/Password**
  - Buat user pertama: email `centralvex@noir.local`, password sesuai keinginanmu
    (jangan pakai password lama yang sudah pernah kamu tulis di chat ini — anggap
    sudah bocor, ganti dengan yang baru)
  - Di **Realtime Database**, buat node awal secara manual:
    ```json
    "users": {
      "<UID user centralvex dari Authentication>": {
        "username": "centralvex",
        "role": "owner"
      }
    }
    ```
  - Import rules dari `firebase/database-structure.md` ke tab **Rules**.

### 2. Admin Panel (web)
- Buka folder `admin-panel/` dengan live server apa saja (VS Code Live Server,
  `python3 -m http.server`, Firebase Hosting, dll) — tidak bisa dibuka langsung
  sebagai `file://` karena Firebase Auth butuh HTTP(S).
- Taruh file banner kamu sebagai `admin-panel/banner.jpg`.

### 3. App Anak (Android)
- Buka folder `child-app-android/` di Android Studio.
- Download `google-services.json` dari Firebase Console (Project Settings > Your apps >
  Add Android app, pakai package name `com.parentalcontrol.app`) dan taruh di
  `child-app-android/app/`.
- Build & install ke HP anak lewat Android Studio (USB debugging), karena app ini
  tidak akan lolos review Play Store akibat fitur Device Admin + location background.
- Saat pertama buka, app akan minta izin lokasi & Device Admin — ini akan terlihat
  jelas oleh anak/pemegang HP sebagai dialog sistem Android biasa.

## Catatan jujur

Skeleton ini mencakup struktur inti & fitur utama, tapi untuk app Android yang siap
pakai masih perlu kamu lengkapi: UI polish, icon custom, testing di device asli,
penanganan izin runtime (runtime permission request untuk lokasi & notifikasi),
dan halaman "masukkan password ortu" sebelum Device Admin dinonaktifkan.
