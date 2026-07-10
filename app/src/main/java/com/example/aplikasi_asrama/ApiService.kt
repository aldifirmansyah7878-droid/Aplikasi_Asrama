package com.example.aplikasi_asrama.api

import RegisterRequest
import com.example.aplikasi_asrama.KamarData
import com.example.aplikasi_asrama.api.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse>

    // ==================== USERS (dropdown admin) ====================
    @GET("get_users.php")
    suspend fun getUsers(): Response<UserListResponse>

    // ==================== PENGHUNI ====================
    @GET("get_penghuni.php")
    suspend fun getAllPenghuni(): Response<PenghuniResponse>

    @GET("get_penghuni_by_user.php")
    suspend fun getPenghuniByUserId(@Query("user_id") userId: Int): Response<PenghuniResponse>

    @GET("get_penghuni.php")
    suspend fun getPenghuniById(@Query("id") id: Int): Response<PenghuniResponse>

    @GET("tambah_penghuni.php")
    suspend fun tambahPenghuniGet(
        @Query("userId") userId: Int,
        @Query("nama") nama: String,
        @Query("Nim") nim: String,
        @Query("noTelp") noTelp: String,
        @Query("alamatAsal") alamat: String,
        @Query("kamarId") kamarId: Int,
        @Query("nomorKamar") nomorKamar: String,
        @Query("tanggalMasuk") tanggalMasuk: String,
        @Query("tanggalKeluar") tanggalKeluar: String?,
        @Query("status") status: String
    ): Response<BaseResponse>

    @GET("edit_penghuni.php")
    suspend fun editPenghuniGet(
        @Query("id") id: Int,
        @Query("userId") userId: Int,
        @Query("nama") nama: String,
        @Query("Nim") nim: String,
        @Query("noTelp") noTelp: String,
        @Query("alamatAsal") alamat: String,
        @Query("kamarId") kamarId: Int,
        @Query("nomorKamar") nomorKamar: String,
        @Query("tanggalMasuk") tanggalMasuk: String,
        @Query("tanggalKeluar") tanggalKeluar: String?,
        @Query("status") status: String
    ): Response<BaseResponse>

    @POST("hapus_penghuni.php")
    suspend fun hapusPenghuni(@Body request: Map<String, Int>): Response<BaseResponse>

    @Multipart
    @POST("update_foto_penghuni.php")
    suspend fun updateFotoPenghuni(
        @Part("id") id: Int,
        @Part foto: MultipartBody.Part?
    ): Response<BaseResponse>

    // ==================== KAMAR ====================
    @GET("get_kamar.php")
    suspend fun getAllKamar(): Response<KamarResponse>

    @GET("get_kamar.php")
    suspend fun getKamarById(@Query("id") id: Int): Response<KamarResponse>

    @POST("tambah_kamar.php")
    suspend fun tambahKamar(@Body kamar: KamarData): Response<BaseResponse>

    @POST("edit_kamar.php")
    suspend fun editKamar(@Body kamar: KamarData): Response<BaseResponse>

    @POST("hapus_kamar.php")
    suspend fun hapusKamar(@Body request: Map<String, Int>): Response<BaseResponse>

    // ==================== IZIN ====================
    @POST("tambah_izin.php")
    suspend fun createIzinKeluar(@Body request: IzinKeluarRequest): Response<IzinResponse>

    @GET("get_izin.php")
    suspend fun getAllIzinRest(
        @Query("status") status: String? = null,
        @Query("user_id") userId: Int? = null
    ): Response<IzinListResponse>

    @GET("get_izin_detail.php")
    suspend fun getIzinByIdRest(@Query("id") id: Int): Response<IzinResponse>

    @PUT("put_izin_kembali.php")
    suspend fun returnIzin(@Body request: Map<String, Int>): Response<IzinResponse>

    @PUT("put_izin_update.php")
    suspend fun updateIzin(@Body request: Map<String, Any>): Response<IzinResponse>

    @DELETE("delete_izin.php")
    suspend fun deleteIzin(@Query("id") id: Int): Response<BaseResponse>

    @POST("update_status_izin.php")
    suspend fun updateStatusIzin(@Body request: Map<String, String>): Response<BaseResponse>

    // ==================== TRACKING ====================
    @POST("tracking_update.php")
    suspend fun sendTracking(@Body request: TrackingRequest): Response<BaseResponse>




    // ==================== PAKET ====================
    @GET("get_paket.php")
    suspend fun getAllPaket(): Response<PaketResponse>

    @GET("get_paket_by_user.php")
    suspend fun getPaketByUser(@Query("user_id") userId: Int): Response<PaketResponse>

    @GET("get_paket_by_id.php")
    suspend fun getPaketById(@Query("id") id: Int): Response<PaketResponse>

    @Multipart
    @POST("tambah_paket.php")
    suspend fun tambahPaket(
        @Part("userid") userid: RequestBody,
        @Part("namaPenghuni") namaPenghuni: RequestBody,
        @Part("nomorKamar") nomorKamar: RequestBody,
        @Part("namaPengirim") namaPengirim: RequestBody,
        @Part("jenisPaket") jenisPaket: RequestBody,
        @Part("tanggalDatang") tanggalDatang: RequestBody,
        @Part("STATUS") status: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<ResponseBody>

    @POST("update_status_paket.php")
    suspend fun updateStatusPaket(@Body request: UpdateStatusRequest): Response<BaseResponse>

    @POST("hapus_paket.php")
    suspend fun hapusPaket(@Body request: HapusPaketRequest): Response<BaseResponse>


    // ==================== PEMBAYARAN ====================
    @GET("get_pembayaran.php")
    suspend fun getPembayaran(
        @Query("penghuni_id") penghuniId: Int? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): Response<PembayaranResponse>

    @GET("get_pembayaran_admin.php")
    suspend fun getPembayaranAdmin(
        @Query("status") status: String? = null
    ): Response<PembayaranResponse>

    @GET("get_pembayaran_user.php")
    suspend fun getPembayaranUser(
        @Query("penghuni_id") penghuniId: Int
    ): Response<PembayaranResponse>

    @GET("get_pembayaran_by_id.php")
    suspend fun getPembayaranById(@Query("id") id: Int): Response<PembayaranResponse>

@FormUrlEncoded
@POST("tambah_pembayaran_admin.php")
suspend fun tambahPembayaranAdmin(
    @Field("penghuni_id") penghuni_Id: Int,
    @Field("bulanTahun") bulanTahun: String,
    @Field("jumlah") jumlah: Int,
    @Field("metodePembayaran") metodePembayaran: String
): Response<BaseResponse>
    @Multipart
    @POST("tambah_pembayaran_user.php")
    suspend fun tambahPembayaranUser(
        @Part("penghuni_id") penghuniId: RequestBody,
        @Part("bulanTahun") bulanTahun: RequestBody,
        @Part("jumlah") jumlah: RequestBody,
        @Part("metodePembayaran") metodePembayaran: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("verifikasi_pembayaran.php")
    suspend fun verifikasiPembayaran(
        @Field("id") id: Int,
        @Field("status") status: String,
        @Field("catatan") catatan: String,
        @Field("admin") admin: String
    ): Response<BaseResponse>

    @Multipart
    @POST("upload_bukti_pembayaran.php")
    suspend fun kirimBuktiPembayaran(
        @Part("id") id: Int,
        @Part bukti: MultipartBody.Part
    ): Response<BaseResponse>

    // ==================== TAMU ====================
    @GET("get_tamu.php")
    suspend fun getAllTamu(@Query("STATUS") status: String? = null): Response<TamuResponse>

    @GET("get_tamu_by_user.php")
    suspend fun getTamuByUser(@Query("userid") userid: Int): Response<TamuResponse>  // <-- userid

    @GET("get_tamu_detail.php")
    suspend fun getTamuById(@Query("id") id: Int): Response<TamuDetailResponse>

    @POST("tambah_tamu.php")
    suspend fun tambahTamu(@Body request: TamuRequest): Response<BaseResponse>

    @FormUrlEncoded
    @POST("update_status_tamu.php")
    suspend fun updateStatusTamu(
        @Field("id") id: Int,
        @Field("status") status: String
    ): Response<BaseResponse>

    @FormUrlEncoded
    @POST("hapus_tamu.php")
    suspend fun hapusTamu(
        @Field("id") id: Int
    ): Response<BaseResponse>


    // ==================== KELUHAN ====================
    @GET("get_keluhan.php")
    suspend fun getAllKeluhan(@Query("status") status: String? = null): Response<KeluhanResponse>

    @GET("get_keluhan_by_user.php")
    suspend fun getKeluhanByUser(@Query("user_id") userId: Int): Response<KeluhanResponse>

    @GET("get_keluhan_detail.php")
    suspend fun getKeluhanById(@Query("id") id: Int): Response<KeluhanDetailResponse>

    @Multipart
    @POST("tambah_keluhan.php")
    suspend fun tambahKeluhan(
        @Part("userId") userId: RequestBody,
        @Part("namaPenghuni") namaPenghuni: RequestBody,
        @Part("nomorKamar") nomorKamar: RequestBody,
        @Part("judulKeluhan") judulKeluhan: RequestBody,
        @Part("deskripsiKeluhan") deskripsiKeluhan: RequestBody,
        @Part("tanggalKeluhan") tanggalKeluhan: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<BaseResponse>

    @Multipart
    @POST("tanggapi_keluhan.php")
    suspend fun tanggapiKeluhanWithFoto(
        @Part("id") id: Int,
        @Part("tanggapan") tanggapan: String,
        @Part("status") status: String,
        @Part fotoPerbaikan: MultipartBody.Part?
    ): Response<BaseResponse>

    @POST("update_status_keluhan.php")
    suspend fun updateStatusKeluhan(
        @Body request: Map<String, String>
    ): Response<BaseResponse>

    @POST("hapus_keluhan.php")
    suspend fun hapusKeluhan(@Body request: Map<String, Int>): Response<BaseResponse>

    // ==================== DASHBOARD ====================
    @GET("get_dashboard_overview.php")
    suspend fun getDashboardOverview(): Response<DashboardOverviewResponse>


    @POST("save_notifikasi.php")
    suspend fun saveNotifikasi(@Body request: Map<String, String>): Response<BaseResponse>

    @GET("get_notifikasi.php")
    suspend fun getNotifikasi(@Query("user_id") userId: Int): Response<NotifikasiResponse>

    @POST("mark_read_notifikasi.php")
    suspend fun markReadNotifikasi(@Body request: Map<String, String>): Response<BaseResponse>

}