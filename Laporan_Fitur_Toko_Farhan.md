# Laporan Fitur dan Fungsi Tombol Aplikasi Toko Farhan POS

Aplikasi Toko Farhan POS adalah sistem Point of Sale offline-first yang dibangun menggunakan Android Native (Kotlin & Jetpack Compose) dengan database lokal SQLite (Room). Aplikasi ini terbagi menjadi 4 menu utama yang dapat diakses melalui Navigation Bar di bagian bawah layar.

## 1. Menu Transaksi (PosScreen)
Layar ini adalah antarmuka utama kasir untuk melakukan transaksi penjualan sehari-hari.

### Fitur Utama:
*   **Pencarian Barang:** Fitur pencarian real-time berdasarkan nama barang atau barcode.
*   **Katalog Barang:** Menampilkan daftar barang dalam bentuk kartu (card) lengkap dengan informasi nama, harga jual, dan sisa stok.
*   **Manajemen Keranjang (Cart):** Menambahkan, mengurangi, atau menghapus item dari keranjang belanja.
*   **Peringatan Stok Menipis:** Indikator visual (badge merah) dan dialog pop-up yang memberi tahu kasir jika ada barang yang stoknya mencapai batas minimum.

### Penjelasan Tombol:
*   **Ikon Notifikasi (Lonceng):** Membuka dialog "Stok Mau Habis" untuk melihat daftar barang yang stoknya kritis (berada di bawah atau sama dengan batas minimum peringatan). Jika ada barang kritis, ikon akan menampilkan *badge* merah dengan jumlah barang.
*   **Ikon Keranjang:** Membuka panel bawah (Bottom Sheet) keranjang belanja yang berisi rincian item yang akan dibayar. Terdapat *badge* angka yang menunjukkan total kuantitas barang di keranjang.
*   **Kolom Pencarian ("Cari atau scan barang..."):** Input teks untuk mencari barang.
*   **Ikon Silang (Clear) di Pencarian:** Menghapus teks pencarian agar kembali menampilkan seluruh barang.
*   **Tombol `+` (Tambah) pada Kartu Barang:** Menambahkan 1 unit barang tersebut ke dalam keranjang belanja. Tombol ini muncul jika barang belum ada di keranjang.
*   **Tombol `+` dan `-` pada Kartu Barang (saat di keranjang):** Menambah atau mengurangi kuantitas barang tersebut di dalam keranjang.
*   **Tombol "BAYAR SEKARANG":** Membuka Bottom Sheet keranjang belanja. Tombol ini hanya muncul di bagian bawah layar jika keranjang tidak kosong.

### Bottom Sheet Keranjang (CartSheet) & Pembayaran:
*   **Tombol "Hapus Semua" (Keranjang):** Mengosongkan seluruh isi keranjang belanja.
*   **Tombol `X` (Tutup Keranjang):** Menutup panel keranjang belanja.
*   **Ikon Tong Sampah (Hapus Item):** Menghapus satu jenis barang tertentu dari keranjang.
*   **Tombol Edit Harga (Ikon Pensil):** Mengizinkan kasir untuk mengubah harga jual satuan barang secara *custom* (misal: memberikan diskon khusus pada transaksi ini).
*   **Tombol "Pilih Pembayaran":** Membuka dialog konfirmasi pembayaran.
*   **Filter Chip Metode Bayar ("CASH", "QRIS", "TRANSFER"):** Memilih metode pembayaran yang digunakan pelanggan.
*   **Kolom "Jumlah Bayar":** Input nominal uang yang dibayarkan oleh pelanggan.
*   **Chip Nominal Cepat (Suggestion Chip):** Tombol-tombol yang berisi saran nominal pembayaran (seperti uang pas, kelipatan 10.000, 50.000, atau 100.000) untuk mempercepat input kasir.
*   **Tombol "Batal" (Pembayaran):** Menutup dialog pembayaran tanpa memproses transaksi.
*   **Tombol "Bayar":** Memproses transaksi, mengurangi stok, menyimpan ke database, dan mengarahkan ke layar Sukses. Tombol ini hanya aktif jika jumlah bayar >= total belanja.

---

## 2. Menu Riwayat (HistoryScreen)
Layar ini menampilkan rekam jejak seluruh transaksi yang pernah dilakukan.

### Fitur Utama:
*   **Daftar Riwayat:** Menampilkan daftar transaksi yang dikelompokkan berdasarkan tanggal.
*   **Filter Tanggal:** Memungkinkan pengguna melihat transaksi pada tanggal tertentu saja.
*   **Detail Transaksi:** Menampilkan informasi lengkap setiap transaksi, termasuk barang yang dibeli, harga, total, uang yang dibayar, dan kembalian.
*   **Cetak & Bagikan Struk:** Fitur untuk mencetak struk ke format PDF atau membagikannya melalui WhatsApp.

### Penjelasan Tombol:
*   **Ikon Kalender (Filter Tanggal):** Membuka dialog kalender (Date Picker) untuk memilih tanggal tertentu guna memfilter riwayat transaksi.
*   **Ikon Filter Coret (Hapus Filter) / Tombol `X` pada Chip Filter:** Menghapus filter tanggal yang sedang aktif sehingga menampilkan semua riwayat kembali.
*   **Kartu Transaksi (Klik pada baris):** Membuka panel bawah (Bottom Sheet) yang berisi rincian detail transaksi tersebut.
*   **Tombol `X` (Tutup Detail):** Menutup panel detail transaksi.
*   **Tombol "Cetak Struk":** Membuat file PDF struk belanja dari transaksi tersebut dan membuka dialog sistem untuk mencetak (Print) atau menyimpannya.
*   **Tombol "Kirim WA":** Menghasilkan teks struk belanja dan otomatis membuka aplikasi WhatsApp untuk dikirimkan kepada pelanggan.

---

## 3. Menu Stok (StockScreen)
Layar ini adalah pusat manajemen inventaris untuk menambah barang baru, mengedit, menghapus, serta melakukan restock (pembelian stok masuk). Menu ini memiliki 2 Tab: "Daftar Barang" dan "Beli Stok Masuk".

### Tab Daftar Barang
*   **Kolom Pencarian:** Untuk mencari barang di daftar inventaris.
*   **Ikon Refresh (Top Bar):** Memuat ulang data barang dari database.
*   **Ikon Pensil (Edit Harga):** Membuka dialog untuk mengubah harga jual default barang tersebut.
*   **Ikon Tong Sampah (Hapus):** Menghapus barang secara permanen dari sistem (akan muncul dialog konfirmasi).
*   **Tombol Floating Action Button `+` (Tambah Barang):** Membuka dialog untuk mendaftarkan barang baru ke dalam sistem.

#### Dialog Tambah Barang Baru
*   **Input "Nama Barang":** Mengisi nama produk.
*   **Input "Barcode":** (Opsional) Mengisi kode barcode.
*   **Input "Harga Jual (Rp)":** Mengatur harga jual eceran.
*   **Input "Satuan Beli" & "Satuan Jual":** Mengatur satuan grosir (misal: Dus) dan satuan ecer (misal: Pcs).
*   **Input "Rasio Konversi":** Berapa banyak satuan jual dalam satu satuan beli (misal: 1 Dus = 24 Pcs, maka rasio = 24).
*   **Input "Min. Stok Alert":** Batas minimum stok sebelum sistem memberikan peringatan stok menipis.
*   **Tombol "Simpan":** Menyimpan data barang baru.
*   **Tombol "Batal":** Menutup dialog.

### Tab Beli Stok Masuk (Restock)
Fitur ini menggunakan sistem HPP (Harga Pokok Penjualan) *Moving Average*. Saat stok masuk, sistem otomatis mengonversi satuan grosir ke eceran dan menghitung ulang rata-rata modal.
*   **Kolom Pencarian Barang:** Mencari barang yang akan di-restock.
*   **Baris Hasil Pencarian (Klik):** Memilih barang tersebut untuk diisi stoknya. Jika barang tidak ada, muncul tombol "Tambah Barang Baru".
*   **Ikon Silang (Close) pada Barang Terpilih:** Membatalkan pilihan barang.
*   **Input "Jumlah Beli":** Mengisi berapa banyak unit (dalam satuan grosir/beli) yang disuplai.
*   **Input "Total Harga Modal":** Mengisi total biaya kulakan (modal) untuk pembelian stok tersebut.
*   **Tombol "Simpan Stok Baru":** Memproses stok masuk, mengonversi ke satuan jual, mengupdate HPP barang secara *Moving Average*, dan menambah kuantitas stok di database.

---

## 4. Menu Laporan (ReportScreen)
Layar ini menyediakan analisis performa toko berupa ringkasan pendapatan, laba/rugi, dan grafik penjualan.

### Fitur Utama:
*   **Dashboard Ringkasan:** Menampilkan Penjualan Hari Ini, Laba Hari Ini, Total Laba (seluruh waktu/filter), dan peringatan Item Stok Hampir Habis.
*   **Grafik Penjualan:** Visualisasi *Bar Chart* yang menampilkan tren pendapatan per hari.
*   **Tabel Rincian Harian:** Menampilkan rekapitulasi jumlah transaksi, pendapatan, dan laba per tanggal.
*   **Ekspor Excel:** Menghasilkan laporan detail transaksi berformat Excel (.xls).

### Penjelasan Tombol:
*   **Ikon Kalender & Filter:** Sama seperti di menu Riwayat, berfungsi untuk memfilter data laporan berdasarkan tanggal tertentu.
*   **Ikon Refresh:** Memperbarui data laporan dari database.
*   **Tombol "Simpan Excel":** Menggabungkan seluruh data transaksi (dikelompokkan per bulan dalam sheet Excel), lalu menyimpannya langsung ke folder `Downloads` di perangkat.
*   **Tombol "Bagikan":** Membuat file laporan Excel dan langsung membuka menu berbagi (Share) bawaan Android (misal dikirim via WhatsApp, Email, dll).

---

## 5. Layar Sukses (SuccessScreen)
Layar ini muncul tepat setelah kasir berhasil menyelesaikan pembayaran pada menu Transaksi.

### Fitur Utama:
*   Menampilkan ringkasan singkat transaksi yang baru saja terjadi (Waktu, No. Struk, Total Belanja, Tunai, Kembalian).

### Penjelasan Tombol:
*   **Tombol "Cetak Struk":** Membuat file PDF struk belanja dan membukanya untuk dicetak.
*   **Tombol "Kirim WA":** Menghasilkan teks struk belanja dan mengarahkan ke WhatsApp untuk dikirim.
*   **Tombol "Mulai Transaksi Baru":** Menutup layar sukses dan mengembalikan kasir ke menu utama (PosScreen) dengan keranjang yang sudah bersih, siap melayani pelanggan berikutnya.
