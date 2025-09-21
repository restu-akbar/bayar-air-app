package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.com.bayarair.data.dto.LoginRequest
import org.com.bayarair.data.dto.LoginResponse
import org.com.bayarair.data.remote.BASE_URL
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.dto.HargaData
import org.com.bayarair.data.dto.PelangganDto
import org.com.bayarair.data.dto.SaveRecordResponse
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.model.toDomain
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement
import org.com.bayarair.data.model.MeterRecord

class MeterRecordRepository(
    private val client: HttpClient
) {
    suspend fun getRecords(): Result<List<MeterRecord>> = runCatching {
        val res = client.get("$BASE_URL/records") {
            contentType(ContentType.Application.Json)
        }
        res.body<List<MeterRecord>>()
    }
}