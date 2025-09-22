package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.contentType
import org.com.bayarair.data.dto.ApiException
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.remote.BASE_URL
import org.com.bayarair.data.model.MeterRecord

class MeterRecordRepository(
    private val client: HttpClient
) {
    suspend fun getRecords(): Result<List<MeterRecord>> = runCatching {
        val res = client.get("$BASE_URL/records") {
            contentType(ContentType.Application.Json)
        }
        res.unwrapFlexible<List<MeterRecord>>()
    }.mapCatching { env ->
        when {
            env.status && env.data != null -> env.data
            else -> throw ApiException(200, env.message.ifBlank { "Gagal memuat data History" })
        }
    }
}