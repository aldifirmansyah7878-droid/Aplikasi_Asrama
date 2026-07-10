//package com.example.aplikasi_asrama.api.model
//
//data class BaseResponse(
//    val success: Boolean = false,
//    val status: String? = null,
//    val message: String? = null,
//    val error: String? = null,
//    val debug: Map<String, Any>? = null
//)

package com.example.aplikasi_asrama.api.model

data class BaseResponse(
    val status: String,                    // "success" / "error"
    val message: String? = null,
    val error: String? = null,
    val debug: Map<String, Any>? = null
) {
    // Helper untuk cek sukses (baik dari status atau success)
    fun isSuccess(): Boolean = status == "success"
}