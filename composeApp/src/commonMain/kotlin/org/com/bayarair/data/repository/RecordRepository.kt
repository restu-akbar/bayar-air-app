package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.com.bayarair.data.dto.ApiException
import org.com.bayarair.data.dto.BaseResponse
import org.com.bayarair.data.dto.HargaData
import org.com.bayarair.data.dto.PelangganDto
import org.com.bayarair.data.dto.SaveRecordResponse
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.model.toDomain
import org.com.bayarair.data.remote.BASE_URL

class RecordRepository(
    private val client: HttpClient,
) {
    suspend fun getHarga(): Result<HargaData> =
        runCatching {
            val res = client.get("$BASE_URL/harga") { accept(ContentType.Application.Json) }
            res.unwrapFlexible<HargaData>()
        }.mapCatching { env ->
            when {
                env.status && env.data != null -> env.data
                else -> throw ApiException(200, env.message.ifBlank { "Gagal memuat data harga" })
            }
        }

    suspend fun getPelanggan(): Result<List<PelangganDto>> =
        runCatching {
            val res = client.get("$BASE_URL/pelanggan") { accept(ContentType.Application.Json) }
            res.unwrapFlexible<List<PelangganDto>>()
        }.mapCatching { env ->
            when {
                env.status && env.data != null -> env.data
                else -> throw ApiException(200, env.message.ifBlank { "Gagal memuat data pelanggan" })
            }
        }

    suspend fun getCustomers(): Result<List<Customer>> = getPelanggan().mapCatching { list -> list.map { it.toDomain() } }

    suspend fun saveRecord(
        customerId: String,
        meter: Int,
        totalAmount: Long,
        evidence: ByteArray,
        otherFees: Map<String, Long?>,
    ): Result<BaseResponse<SaveRecordResponse>> =
        runCatching {
            client
                .submitFormWithBinaryData(
                    url = "$BASE_URL/pencatatan",
                    formData =
                        formData {
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
                                        "filename=\"meter.jpg\"",
                                    )
                                },
                            )
                        },
                ).unwrapFlexible<SaveRecordResponse>()
        }

    suspend fun updateRecord(recordId: String): Result<BaseResponse<SaveRecordResponse>> =
        runCatching {
            val res =
                client.patch("$BASE_URL/pencatatan/$recordId") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("status" to "sudah_bayar"))
                }
            res.unwrapFlexible<BaseResponse<SaveRecordResponse>>()
        }.mapCatching { env ->
            when {
                env.status && env.data != null -> env.data
                else -> throw ApiException(
                    200,
                    env.message.ifBlank { "Gagal memperbarui status" },
                )
            }
        }
}
