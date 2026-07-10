# Aplikasi Asrama

Aplikasi manajemen asrama berbasis Android dengan backend REST API.

## Fitur Utama
- Login & Registrasi (Admin/User)
- Manajemen Penghuni, Kamar, Paket, Tamu
- Izin Keluar/Masuk dengan Tracking Lokasi
- Keluhan & Tanggapan Admin
- Pembayaran Sewa (Upload Bukti & Verifikasi)
- Notifikasi Real-time

## Tech Stack
- **Frontend**: Android (Kotlin)
- **Backend**: PHP (REST API)
- **Database**: MySQL
- **Library**: Retrofit, Glide, ViewModel, LiveData, Coroutines

## Cara Build APK
1. Clone repository:
   ```bash
   git clone https://github.com/aldifirmansyah7878-droid/Aplikasi_Asrama.git
2. Buka project di Android Studio.
3. Tunggu hingga gradle sync selesai.
4. Pilih Build → Build Bundle(s) / APK(s) → Build APK(s).
5. Hasil APK ada di app/build/outputs/apk/debug/.

Konfigurasi Backend
1. Upload folder aplikasi_asrama_backend ke server (XAMPP/hosting).
2. Import database dari file SQL.
3. Edit config/database.php sesuai kredensial server.
4. Ubah BASE_URL di RetrofitClient.kt di Android sesuai IP server.

Login Default
Role	Username	Password
Admin	admin	(sesuai database)
User	user	(sesuai database)
