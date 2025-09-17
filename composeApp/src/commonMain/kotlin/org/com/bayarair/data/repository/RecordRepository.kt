package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import org.com.bayarair.data.dto.LoginRequest
import org.com.bayarair.data.dto.LoginResponse
import org.com.bayarair.data.remote.BASE_URL
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.dto.HargaData
import org.com.bayarair.data.dto.PelangganDto
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.model.toDomain

import kotlinx.serialization.json.JsonObject

class RecordRepository(private val client: HttpClient) {
    suspend fun getHarga(): Result<Long> = runCatching {
        val res = client.get("$BASE_URL/harga") {
            accept(ContentType.Application.Json)
        }
        res.unwrapFlexible<HargaData>().harga
    }

    suspend fun getPelanggan(): Result<List<PelangganDto>> = runCatching {
        val res = client.get("$BASE_URL/pelanggan") {
            accept(ContentType.Application.Json)
        }
        res.unwrapFlexible<List<PelangganDto>>()
    }

    suspend fun getCustomers(): Result<List<Customer>> =
        getPelanggan().mapCatching { list -> list.map { it.toDomain() } }
}
