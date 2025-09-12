package org.com.bayarair.data.repository
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import org.com.bayarair.data.dto.LoginRequest
import org.com.bayarair.data.dto.LoginResponse
import org.com.bayarair.data.remote.BASE_URL
import org.com.bayarair.data.dto.ensureOk
import org.com.bayarair.data.dto.unwrapFlexible
import kotlinx.serialization.json.JsonObject

class AuthRepository(private val client: HttpClient) {

    // GET /api/auth/validate  -> { status, message, data: null }
    suspend fun isTokenValid(token: String): Boolean {
        val res = client.get("$BASE_URL/api/validate-token") {
            bearerAuth(token)
            accept(ContentType.Application.Json)
        }
        // jika endpoint tak kirim data: cukup pastikan status=true
        res.ensureOk()
        return true
    }

    // POST /api/login -> { status, message, data: { token } }
    suspend fun login(login: String, password: String): Result<String> = runCatching {
        val res = client.post("$BASE_URL/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(login, password))
        }

        res.unwrapFlexible<LoginResponse>().token
    }
}
