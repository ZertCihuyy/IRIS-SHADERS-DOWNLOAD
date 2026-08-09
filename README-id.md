# IRIS SHEDERS DOWNLOAD

<p align="center">
  <img src="src/main/resources/assets/iris_shaders_download/icon.png" alt="Mod Icon" width="128">
</p>

Selamat datang di **IRIS SHEDERS DOWNLOAD**! Ini adalah mod Minecraft Fabric untuk versi 1.21.1 yang dirancang untuk mengotomatisasi penuh pengalaman instalasi shader Anda.

## Fitur
- **Download Otomatis (Auto-Download)**: Jika Anda belum menginstal shader pack, mod ini akan secara otomatis mengunduhnya dari Modrinth langsung ke folder `.minecraft/shaderpacks` Anda. Anda tidak perlu memindahkan file secara manual!
- **Terapkan Otomatis (Auto-Apply)**: Mod ini akan otomatis mengubah konfigurasi `iris.properties` Anda untuk memastikan bahwa Iris Shaders diaktifkan dan shader pack yang baru diunduh langsung dipilih saat Anda membuka game.
- **Siap Menggunakan GitHub Actions**: Repositori ini sudah dilengkapi alur kerja (workflow) GitHub Actions otomatis (`.github/workflows/release.yml`). Jika Anda melakukan push dengan tag versi seperti `v1.0.0`, GitHub akan otomatis melakukan kompilasi dan merilis file `.jar` yang siap pakai.

## Penjelasan Kode Lengkap (Full Code Explanation)
Logika utama dari mod ini berada pada file `IrisShadersDownloadClient.java` yang berjalan pada tahap `ClientModInitializer`.
1. **Pemeriksaan Direktori**: Kode akan mengecek apakah folder `shaderpacks` ada di dalam direktori Minecraft, jika tidak ada maka akan otomatis dibuat.
2. **Eksekusi Unduhan**: Kode melakukan request HTTP GET ke API Modrinth untuk mencari URL unduhan shader terbaru, lalu mengunduh file `.zip` tersebut.
3. **Injeksi Konfigurasi**: Kode akan membaca file `config/iris.properties`, memperbarui nilai `enableShaders` menjadi `true` dan menetapkan `shaderPack` ke shader yang diunduh, lalu menyimpannya. Hal ini memaksa shader langsung aktif tanpa interaksi pengguna.

## Lisensi
Proyek ini dilisensikan di bawah **GPLv3** (GNU General Public License v3.0). Anda bebas untuk memodifikasi, mendistribusikan, dan menggunakan kode ini asalkan perubahan Anda juga bersifat open source di bawah lisensi yang sama.

## Cara Menggunakan
1. Masukkan file `.jar` mod ini ke dalam folder `.minecraft/mods` Anda.
2. Buka Minecraft (pastikan Fabric API dan Iris sudah terinstal).
3. Masuk ke dunia Anda, dan nikmati shader baru yang indah secara instan!

---
*Catatan: Code ini kurang optimal karena dibuat menggunakan AI dll.*
