package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.com.bayarair.data.dto.BarChart
import org.com.bayarair.data.dto.BaseResponse
import org.com.bayarair.data.dto.HargaData
import org.com.bayarair.data.dto.PieChart
import org.com.bayarair.data.dto.SaveRecordResponse
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.model.MeterRecord
import org.com.bayarair.data.remote.BASE_URL

class RecordRepository(
    private val client: HttpClient,
) {
    suspend fun getRecords(): Result<List<MeterRecord>> =
        runCatching {
            val res =
                client.get("$BASE_URL/pencatatan") {
                    contentType(ContentType.Application.Json)
                }
            res.unwrapFlexible<List<MeterRecord>>().data!!
        }

    suspend fun getHarga(): Result<HargaData> =
        runCatching {
            val res = client.get("$BASE_URL/harga")
            res.unwrapFlexible<HargaData>().data!!
        }


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
                    setBody(mapOf("status" to "sudah_bayar"))
                }
            res.unwrapFlexible<BaseResponse<SaveRecordResponse>>().data!!
        }

    suspend fun getMonthlyStats(month: Int): Result<PieChart> =
        runCatching {
            val res =
                client.get("$BASE_URL/statistik/pie-chart") {
                    parameter("bulan", month)
                }
            res.unwrapFlexible<PieChart>().data!!
        }

    suspend fun getYearlyStats(year: Int): Result<BarChart> =
        runCatching {
            val res =
                client.get("$BASE_URL/statistik/bar-chart") {
                    parameter("tahun", year)
                }
            res.unwrapFlexible<BarChart>().data!!
        }
}
