data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(
    val username: String,
    val password: String,
    val namaLengkap: String,
    val role: String = "user"
)