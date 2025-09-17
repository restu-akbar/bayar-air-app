package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
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
        suspend fun saveRecord(
        customerId: String,
        meter: Int,
        totalAmount: Long,
        evidence: ByteArray,
        otherFees: Map<String, Long?>
    ): Result<Unit> = runCatching {
        client.submitFormWithBinaryData(
            url = "$BASE_URL/pencatatan",
            formData = formData {
                append("customer_id", customerId)
                append("meter", meter.toString())
                append("total_amount", totalAmount.toString())

                otherFees["Denda"]?.let { append("fine", it.toString()) }
                otherFees["Materai"]?.let { append("duty_stamp", it.toString()) }
                otherFees["Retribusi"]?.let { append("retribution_fee", it.toString()) }

                append(
                    "evidence",
                    evidence,
                    Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"meter.jpg\""
                        )
                    }
                )
            }
        ).unwrapFlexible<Unit>()
    }
}
