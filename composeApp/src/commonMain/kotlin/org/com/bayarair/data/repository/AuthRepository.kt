package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import org.com.bayarair.data.dto.LoginRequest
import org.com.bayarair.data.dto.LoginResponse
import org.com.bayarair.data.dto.BaseResponse
import org.com.bayarair.data.remote.BASE_URL
import org.com.bayarair.data.dto.unwrapFlexible
import kotlinx.serialization.json.JsonObject

class AuthRepository(private val client: HttpClient) {
    // POST /login -> { status, message, data: { user, token } }
    suspend fun login(login: String, password: String): Result<BaseResponse<LoginResponse>> = runCatching {
        val res = client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(login, password))
        }

        res.unwrapFlexible<LoginResponse>()
      }

    // POST /logout -> { status, message, data: NULL }
    suspend fun logout(): Result<BaseResponse<Unit?>> = runCatching {
        val res = client.post("$BASE_URL/logout")
        res.unwrapFlexible<Unit?>()
    }
}
