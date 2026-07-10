package com.example.aplikasi_asrama.repository

import RegisterRequest
import android.util.Log
import com.example.aplikasi_asrama.DashboardOverview
import com.example.aplikasi_asrama.KamarData
import com.example.aplikasi_asrama.PenghuniData
import com.example.aplikasi_asrama.api.RetrofitClient
import com.example.aplikasi_asrama.api.model.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.collections.firstOrNull

class ApiRepository {

    // ==================== AUTH ====================
    suspend fun login(username: String, password: String): LoginResponse? {
        return try {
            val response = RetrofitClient.instance.login(LoginRequest(username, password))
            Log.d("API_LOGIN", "HTTP Code: ${response.code()}")
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("API_LOGIN", "Exception: ${e.message}", e)
            null
        }
    }

    suspend fun register(username: String, password: String, namaLengkap: String): Boolean {
        return try {
            val response =
                RetrofitClient.instance.register(RegisterRequest(username, password, namaLengkap))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_REGISTER", "Exception: ${e.message}", e)
            false
        }
    }

    // ==================== PENGHUNI ====================
    suspend fun getAllPenghuni(): List<PenghuniData> {
        return try {
            val response = RetrofitClient.instance.getAllPenghuni()
            Log.d("API_PENGHUNI", "HTTP Code getAllPenghuni: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    body.data ?: emptyList()
                } else {
                    Log.e("API_PENGHUNI", "Status not success: ${body?.status}")
                    emptyList()
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_PENGHUNI", "Error getAllPenghuni: ${response.code()} - $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_PENGHUNI", "Exception getAllPenghuni: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getPenghuniById(id: Int): PenghuniData? {
        return try {
            val response = RetrofitClient.instance.getPenghuniById(id)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.firstOrNull()
            } else {
                Log.e(
                    "API_PENGHUNI",
                    "Error getPenghuniById: ${response.code()} - ${response.errorBody()?.string()}"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("API_PENGHUNI", "Exception getPenghuniById: ${e.message}", e)
            null
        }
    }

    suspend fun getPenghuniByUserId(userId: Int): PenghuniData? {
        return try {
            Log.d("API_PENGHUNI", "Calling getPenghuniByUserId with userId: $userId")
            val response = RetrofitClient.instance.getPenghuniByUserId(userId)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.firstOrNull()
            } else {
                Log.e(
                    "API_PENGHUNI",
                    "Error: ${response.code()} - ${response.errorBody()?.string()}"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("API_PENGHUNI", "Exception: ${e.message}", e)
            null
        }
    }

    suspend fun tambahPenghuniJSON(penghuni: PenghuniData): Boolean {
        return try {
            val response = RetrofitClient.instance.tambahPenghuniGet(
                userId = penghuni.userId,
                nama = penghuni.nama,
                nim = penghuni.Nim,
                noTelp = penghuni.noTelp,
                alamat = penghuni.alamatAsal,
                kamarId = penghuni.kamarId,
                nomorKamar = penghuni.nomorKamar,
                tanggalMasuk = penghuni.tanggalMasuk,
                tanggalKeluar = penghuni.tanggalKeluar,
                status = penghuni.status
            )
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API", "tambahPenghuni error", e)
            false
        }
    }

    suspend fun editPenghuniJSON(penghuni: PenghuniData): Boolean {
        return try {
            val response = RetrofitClient.instance.editPenghuniGet(
                id = penghuni.id,
                userId = penghuni.userId,
                nama = penghuni.nama,
                nim = penghuni.Nim,
                noTelp = penghuni.noTelp,
                alamat = penghuni.alamatAsal,
                kamarId = penghuni.kamarId,
                nomorKamar = penghuni.nomorKamar,
                tanggalMasuk = penghuni.tanggalMasuk,
                tanggalKeluar = penghuni.tanggalKeluar,
                status = penghuni.status
            )
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API", "editPenghuni error", e)
            false
        }
    }

    suspend fun hapusPenghuni(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.hapusPenghuni(mapOf("id" to id))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_PENGHUNI", "Exception hapusPenghuni: ${e.message}", e)
            false
        }
    }

    // ==================== UPDATE FOTO PROFIL ====================
    suspend fun updateFotoPenghuni(penghuniId: Int, fotoFile: File?): Boolean {
        return try {
            Log.d("API_FOTO", "updateFotoPenghuni: id=$penghuniId, file=${fotoFile?.absolutePath}")
            if (fotoFile == null || !fotoFile.exists()) {
                Log.e("API_FOTO", "File tidak valid atau tidak ada")
                return false
            }
            val requestFile = fotoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val fotoPart = MultipartBody.Part.createFormData("foto", fotoFile.name, requestFile)
            val response = RetrofitClient.instance.updateFotoPenghuni(penghuniId, fotoPart)
            Log.d("API_FOTO", "Response code: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("API_FOTO", "Response body: $body")
                body?.status == "success"
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_FOTO", "Error: ${response.code()} - $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("API_FOTO", "Exception updateFotoPenghuni", e)
            false
        }
    }

    // ==================== KAMAR ====================
    suspend fun getAllKamar(): List<KamarData> {
        return try {
            val response = RetrofitClient.instance.getAllKamar()
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("API_KAMAR", "Error getAllKamar: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_KAMAR", "Exception getAllKamar: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getKamarById(id: Int): KamarData? {
        return try {
            val response = RetrofitClient.instance.getKamarById(id)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.firstOrNull()
            } else {
                Log.e("API_KAMAR", "Error getKamarById: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_KAMAR", "Exception getKamarById: ${e.message}", e)
            null
        }
    }

    suspend fun tambahKamar(kamar: KamarData): Boolean {
        return try {
            val response = RetrofitClient.instance.tambahKamar(kamar)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_KAMAR", "Exception tambahKamar: ${e.message}", e)
            false
        }
    }

    suspend fun editKamar(kamar: KamarData): Boolean {
        return try {
            val response = RetrofitClient.instance.editKamar(kamar)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_KAMAR", "Exception editKamar: ${e.message}", e)
            false
        }
    }

    suspend fun hapusKamar(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.hapusKamar(mapOf("id" to id))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_KAMAR", "Exception hapusKamar: ${e.message}", e)
            false
        }
    }

    // ==================== IZIN (menggunakan endpoint di folder endpoint/) ====================
    suspend fun getAllIzinRest(status: String? = null): List<IzinData> {
        return try {
            val response = RetrofitClient.instance.getAllIzinRest(status)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                Log.e(
                    "API_IZIN_REST",
                    "Error getAllIzinRest: ${response.code()} - ${response.errorBody()?.string()}"
                )
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_IZIN_REST", "Exception getAllIzinRest: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getIzinByUser(userId: Int): List<IzinData> {
        return try {
            val response = RetrofitClient.instance.getAllIzinRest(userId = userId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("API_IZIN", "Error getIzinByUser: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_IZIN", "Exception getIzinByUser: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getIzinByIdRest(id: Int): IzinData? {
        return try {
            val response = RetrofitClient.instance.getIzinByIdRest(id)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                Log.e("API_IZIN_REST", "Error getIzinByIdRest: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_IZIN_REST", "Exception getIzinByIdRest: ${e.message}", e)
            null
        }
    }

    suspend fun createIzinKeluar(request: IzinKeluarRequest): Boolean {
        return try {
            Log.d("API_IZIN", "Request: ${Gson().toJson(request)}")
            val response = RetrofitClient.instance.createIzinKeluar(request)
            Log.d("API_IZIN", "Response code: ${response.code()}")
            Log.d("API_IZIN", "Response body: ${response.body()}")
            if (!response.isSuccessful) {
                Log.e("API_IZIN", "Error response: ${response.errorBody()?.string()}")
            }
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e("API_IZIN", "Exception", e)
            false
        }
    }

    suspend fun returnIzin(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.returnIzin(mapOf("id" to id))
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e("API_IZIN_REST", "Exception returnIzin: ${e.message}", e)
            false
        }
    }

    suspend fun updateIzin(id: Int, request: UpdateIzinRequest): Boolean {
        return try {
            val map = mutableMapOf<String, Any>("id" to id)
            request.perkiraanKembali?.let { map["perkiraanKembali"] = it }
            request.catatanKunci?.let { map["catatanKunci"] = it }
            request.letakKunci?.let { map["letakKunci"] = it }
            val response = RetrofitClient.instance.updateIzin(map)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e("API_IZIN_REST", "Exception updateIzin: ${e.message}", e)
            false
        }
    }

    suspend fun deleteIzin(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.deleteIzin(id)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_IZIN_REST", "Exception deleteIzin: ${e.message}", e)
            false
        }
    }
    suspend fun updateStatusIzin(id: Int, status: String): Boolean {
        return try {
            // ===== PERBAIKAN: id diubah ke String agar Map<String, String> =====
            val response = RetrofitClient.instance.updateStatusIzin(
                mapOf("id" to id.toString(), "status" to status)
            )
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_IZIN", "updateStatusIzin error", e)
            false
        }
    }

    suspend fun sendTracking(request: TrackingRequest): Boolean {
        return try {
            Log.d(
                "API_TRACKING",
                "Mengirim tracking: izin_id=${request.izin_id}, user_id=${request.user_id}, lat=${request.latitude}, lng=${request.longitude}"
            )
            val response = RetrofitClient.instance.sendTracking(request)
            Log.d("API_TRACKING", "Response code: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("API_TRACKING", "Response body: $body")
                body?.status == "success"
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_TRACKING", "Error: ${response.code()} - $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("API_TRACKING", "Exception", e)
            false
        }
    }

    // ==================== IZIN (mengembalikan data lengkap) ====================
    suspend fun createIzinKeluarWithData(request: IzinKeluarRequest): IzinData? {
        return try {
            val response = RetrofitClient.instance.createIzinKeluar(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                Log.e(
                    "API_IZIN",
                    "createIzinKeluarWithData failed: ${response.code()} - ${
                        response.errorBody()?.string()
                    }"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("API_IZIN", "Exception createIzinKeluarWithData", e)
            null
        }
    }

    // ==================== PAKET (LENGKAP) ====================
    suspend fun getAllPaket(): List<PaketData> {
        val response = RetrofitClient.instance.getAllPaket()
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else emptyList()
    }

    suspend fun getPaketByUser(userId: Int): List<PaketData> {
        val response = RetrofitClient.instance.getPaketByUser(userId)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else emptyList()
    }

    suspend fun getPaketById(id: Int): PaketData? {
        val response = RetrofitClient.instance.getPaketById(id)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data?.firstOrNull()
        } else null
    }


    suspend fun tambahPaket(
        userId: Int,
        namaPenghuni: String,
        nomorKamar: String,
        namaPengirim: String,
        jenisPaket: String,
        tanggalDatang: String,
        status: String,
        fotoFile: File?
    ): Boolean {
        return try {
            val useridBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val namaPenghuniBody = namaPenghuni.toRequestBody("text/plain".toMediaTypeOrNull())
            val nomorKamarBody = nomorKamar.toRequestBody("text/plain".toMediaTypeOrNull())
            val namaPengirimBody = namaPengirim.toRequestBody("text/plain".toMediaTypeOrNull())
            val jenisPaketBody = jenisPaket.toRequestBody("text/plain".toMediaTypeOrNull())
            val tanggalDatangBody = tanggalDatang.toRequestBody("text/plain".toMediaTypeOrNull())
            val statusBody = status.toRequestBody("text/plain".toMediaTypeOrNull())

            val fotoPart = fotoFile?.let {
                val filePart = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("foto", it.name, filePart)
            }

            val response = RetrofitClient.instance.tambahPaket(
                userid = useridBody,
                namaPenghuni = namaPenghuniBody,
                nomorKamar = nomorKamarBody,
                namaPengirim = namaPengirimBody,
                jenisPaket = jenisPaketBody,
                tanggalDatang = tanggalDatangBody,
                status = statusBody,
                foto = fotoPart
            )

            if (response.isSuccessful) {
                val raw = response.body()?.string()
                Log.d("API_PAKET", "Raw response: $raw")
                raw?.let {
                    val json = JSONObject(it)
                    json.getString("status") == "success"
                } ?: false
            } else {
                Log.e("API_PAKET", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("API_PAKET", "tambahPaket error", e)
            false
        }
    }


    suspend fun updateStatusPaket(id: Int, status: String): Boolean {
        return try {
            val response =
                RetrofitClient.instance.updateStatusPaket(UpdateStatusRequest(id, status))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_PAKET", "Exception updateStatusPaket: ${e.message}", e)
            false
        }
    }

    suspend fun hapusPaket(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.hapusPaket(HapusPaketRequest(id))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_PAKET", "hapusPaket error", e)
            false
        }
    }


    // ==================== PEMBAYARAN ====================
    suspend fun getPembayaranUser(penghuniId: Int): List<PembayaranData> {
        val response = RetrofitClient.instance.getPembayaranUser(penghuniId)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else emptyList()
    }

    suspend fun getAllPembayaran(): List<PembayaranData> {
        val response = RetrofitClient.instance.getPembayaran(null, null, null)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else emptyList()
    }

    suspend fun getPembayaranAdmin(status: String? = null): List<PembayaranData> {
        val response = RetrofitClient.instance.getPembayaranAdmin(status)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else emptyList()
    }

    suspend fun searchPembayaran(query: String): List<PembayaranData> {
        val response = RetrofitClient.instance.getPembayaran(null, null, query)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data ?: emptyList()
        } else emptyList()
    }

    suspend fun getPembayaranById(id: Int): PembayaranData? {
        val response = RetrofitClient.instance.getPembayaranById(id)
        return if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data?.firstOrNull()
        } else null
    }

    suspend fun uploadBuktiPembayaran(id: Int, fotoFile: File): Boolean {
        val part = fotoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("bukti", fotoFile.name, part)
        val response = RetrofitClient.instance.kirimBuktiPembayaran(id, body)
        return response.isSuccessful && response.body()?.status == "success"
    }

    suspend fun verifikasiPembayaranAdmin(
        id: Int,
        status: String,
        catatan: String,
        admin: String
    ): Boolean {
        val response = RetrofitClient.instance.verifikasiPembayaran(id, status, catatan, admin)
        return response.isSuccessful && response.body()?.status == "success"
    }

    suspend fun tambahPembayaranAdmin(
        penghuni_Id: Int,
        bulanTahun: String,
        jumlah: Int,
        metode: String
    ): Boolean {
        return try {
            val response = RetrofitClient.instance.tambahPembayaranAdmin(
                penghuni_Id = penghuni_Id,
                bulanTahun = bulanTahun,
                jumlah = jumlah,
                metodePembayaran = metode
            )
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_PEMBAYARAN", "tambahPembayaranAdmin error", e)
            false
        }
    }

    suspend fun tambahPembayaranUser(
        penghuni_Id: Int,
        bulanTahun: String,
        jumlah: Int,
        metode: String,
        file: File
    ): Boolean {
        return try {
            val penghuniBody =
                penghuni_Id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val bulanBody = bulanTahun.toRequestBody("text/plain".toMediaTypeOrNull())
            val jumlahBody = jumlah.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val metodeBody = metode.toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("bukti", file.name, filePart)

            val response = RetrofitClient.instance.tambahPembayaranUser(
                penghuniBody, bulanBody, jumlahBody, metodeBody, body
            )
            val raw = response.body()?.string()
            Log.d("API_UPLOAD", "Raw response: $raw")

            if (response.isSuccessful && raw != null) {
                val json = JSONObject(raw)
                json.getString("status") == "success"
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("API_UPLOAD", "Error: ${e.message}", e)
            false
        }
    }

    suspend fun verifikasiPembayaran(
        id: Int,
        status: String,
        catatan: String,
        admin: String
    ): Boolean {
        val response = RetrofitClient.instance.verifikasiPembayaran(id, status, catatan, admin)
        return response.isSuccessful && response.body()?.status == "success"
    }

    // ==================== TAMU ====================
    suspend fun getAllTamu(status: String? = null): List<TamuData> {
        return try {
            val response = RetrofitClient.instance.getAllTamu(status)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("API_TAMU", "getAllTamu error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TAMU", "getAllTamu exception", e)
            emptyList()
        }
    }

    suspend fun getTamuByUser(userid: Int): List<TamuData> {
        return try {
            Log.d("API_TAMU", "getTamuByUser: userid=$userid")
            val response = RetrofitClient.instance.getTamuByUser(userid)
            Log.d("API_TAMU", "Response code: ${response.code()}")
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else {
                Log.e("API_TAMU", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TAMU", "getTamuByUser exception", e)
            emptyList()
        }
    }

    suspend fun getTamuById(id: Int): TamuData? {
        return try {
            val response = RetrofitClient.instance.getTamuById(id)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data
            } else null
        } catch (e: Exception) {
            Log.e("API_TAMU", "getTamuById exception", e)
            null
        }
    }

    suspend fun tambahTamu(request: TamuRequest): Boolean {
        return try {
            Log.d("API_TAMU", "Mengirim request: $request")
            val response = RetrofitClient.instance.tambahTamu(request)
            Log.d("API_TAMU", "Response code: ${response.code()}")

            // Baca raw body sebagai string untuk debug
            val rawBody = response.errorBody()?.string() ?: response.body()?.toString() ?: "null"
            Log.d("API_TAMU", "Raw response: $rawBody")

            if (response.isSuccessful) {
                val body = response.body()
                body?.status == "success"
            } else {
                Log.e("API_TAMU", "Error response: $rawBody")
                false
            }
        } catch (e: Exception) {
            Log.e("API_TAMU", "tambahTamu exception", e)
            false
        }
    }

    suspend fun updateStatusTamu(id: Int, status: String): Boolean {
        return try {
            val response = RetrofitClient.instance.updateStatusTamu(id, status)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_TAMU", "updateStatusTamu exception", e)
            false
        }
    }

    suspend fun hapusTamu(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.hapusTamu(id)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_TAMU", "hapusTamu exception", e)
            false
        }
    }


    // ==================== KELUHAN ====================
    suspend fun getAllKeluhan(status: String? = null): List<KeluhanData> {
        return try {
            val response = RetrofitClient.instance.getAllKeluhan(status)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "getAllKeluhan error", e)
            emptyList()
        }
    }

    suspend fun getKeluhanByUser(userId: Int): List<KeluhanData> {
        return try {
            val response = RetrofitClient.instance.getKeluhanByUser(userId)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "getKeluhanByUser error", e)
            emptyList()
        }
    }

    suspend fun getKeluhanById(id: Int): KeluhanData? {
        return try {
            val response = RetrofitClient.instance.getKeluhanById(id)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data
            } else null
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "getKeluhanById error", e)
            null
        }
    }

    suspend fun tambahKeluhan(
        userId: Int,
        namaPenghuni: String,
        nomorKamar: String,
        judulKeluhan: String,
        deskripsiKeluhan: String,
        tanggalKeluhan: String,
        fotoFile: File?
    ): Boolean {
        return try {
            Log.d("API_KELUHAN", "userId: $userId, nama: $namaPenghuni, kamar: $nomorKamar")

            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val namaBody = namaPenghuni.toRequestBody("text/plain".toMediaTypeOrNull())
            val kamarBody = nomorKamar.toRequestBody("text/plain".toMediaTypeOrNull())
            val judulBody = judulKeluhan.toRequestBody("text/plain".toMediaTypeOrNull())
            val deskripsiBody = deskripsiKeluhan.toRequestBody("text/plain".toMediaTypeOrNull())
            val tanggalBody = tanggalKeluhan.toRequestBody("text/plain".toMediaTypeOrNull())

            val fotoPart = fotoFile?.let {
                val filePart = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("foto", it.name, filePart)
            }

            val response = RetrofitClient.instance.tambahKeluhan(
                userId = userIdBody,
                namaPenghuni = namaBody,
                nomorKamar = kamarBody,
                judulKeluhan = judulBody,
                deskripsiKeluhan = deskripsiBody,
                tanggalKeluhan = tanggalBody,
                foto = fotoPart
            )

            Log.d("API_KELUHAN", "Response code: ${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("API_KELUHAN", "Response body: $body")
                body?.status == "success"
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("API_KELUHAN", "Error: ${response.code()} - $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "tambahKeluhan error", e)
            false
        }
    }

    suspend fun tanggapiKeluhan(
        id: Int,
        tanggapan: String,
        status: String,
        fotoPerbaikanFile: File? = null
    ): Boolean {
        return try {
            val fotoPart = fotoPerbaikanFile?.let {
                val filePart = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("foto_perbaikan", it.name, filePart)
            }
            val response = RetrofitClient.instance.tanggapiKeluhanWithFoto(
                id = id,
                tanggapan = tanggapan,
                status = status,
                fotoPerbaikan = fotoPart
            )
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "tanggapiKeluhan error", e)
            false
        }
    }

    suspend fun updateStatusKeluhan(id: Int, status: String): Boolean {
        return try {
            val request = mapOf("id" to id.toString(), "status" to status)
            val response = RetrofitClient.instance.updateStatusKeluhan(request)
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "updateStatusKeluhan error", e)
            false
        }
    }

    suspend fun hapusKeluhan(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.hapusKeluhan(mapOf("id" to id))
            response.isSuccessful && response.body()?.status == "success"
        } catch (e: Exception) {
            Log.e("API_KELUHAN", "hapusKeluhan error", e)
            false
        }
    }


    // ==================== USERS ====================
    suspend fun getAllUsers(): List<UserData> {
        return try {
            val response = RetrofitClient.instance.getUsers()
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_USER", "Exception: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun getDashboardOverview(): DashboardOverview? {
        return try {
            val response = RetrofitClient.instance.getDashboardOverview()
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    DashboardOverview(
                        totalPenghuni = apiResponse.totalPenghuni,
                        totalKamar = apiResponse.totalKamar,
                        kamarTersedia = apiResponse.kamarTersedia,
                        totalPendapatan = apiResponse.totalPendapatan.toLong()
                    )
                }
            } else {
                Log.e("API_DASHBOARD", "Error: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_DASHBOARD", "Exception: ${e.message}", e)
            null
        }
    }

    suspend fun saveNotifikasi(userId: Int, judul: String, pesan: String, type: String, referenceId: Int = 0): Boolean {
        return try {
            val request = mapOf(
                "userId" to userId.toString(),
                "judul" to judul,
                "pesan" to pesan,
                "type" to type,
                "reference_id" to referenceId.toString()
            )
            val response = RetrofitClient.instance.saveNotifikasi(request)
            response.isSuccessful && response.body()?.isSuccess() == true
        } catch (e: Exception) {
            Log.e("API_NOTIF", "saveNotifikasi error", e)
            false
        }
    }

    suspend fun getNotifikasi(userId: Int): List<NotifikasiData> {
        return try {
            val response = RetrofitClient.instance.getNotifikasi(userId)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_NOTIF", "getNotifikasi error", e)
            emptyList()
        }
    }

    suspend fun markReadNotifikasi(id: Int): Boolean {
        return try {
            val response = RetrofitClient.instance.markReadNotifikasi(mapOf("id" to id.toString()))
            response.isSuccessful && response.body()?.isSuccess() == true
        } catch (e: Exception) {
            Log.e("API_NOTIF", "markReadNotifikasi error", e)
            false
        }
    }


    // ==================== TEST ====================
    suspend fun testTambahPembayaran(
        penghuniId: Int,
        bulanTahun: String,
        jumlah: Int,
        tanggalBayar: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val client = okhttp3.OkHttpClient()
            val formBody = okhttp3.FormBody.Builder()
                .add("penghuniId", penghuniId.toString())
                .add("bulanTahun", bulanTahun)
                .add("jumlah", jumlah.toString())
                .add("tanggalBayar", tanggalBayar)
                .build()
            val url = RetrofitClient.BASE_URL + "tambah_pembayaran.php"
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(formBody)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            Log.d("TEST_OKHTTP", "HTTP Code: ${response.code}")
            Log.d("TEST_OKHTTP", "Response Body: $body")
            response.isSuccessful && body?.contains("success") == true
        }
    }
}