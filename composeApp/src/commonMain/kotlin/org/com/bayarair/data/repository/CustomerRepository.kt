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
import org.com.bayarair.data.dto.BaseResponse
import org.com.bayarair.data.dto.HargaData
import org.com.bayarair.data.dto.PelangganDto
import org.com.bayarair.data.dto.SaveRecordResponse
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.model.TotalCustomer
import org.com.bayarair.data.model.toDomain
import org.com.bayarair.data.remote.BASE_URL

class CustomerRepository(
    private val client: HttpClient,
) {
    suspend fun getHarga(): Result<HargaData> =
        runCatching {
            val res = client.get("$BASE_URL/harga") { accept(ContentType.Application.Json) }
            res.unwrapFlexible<HargaData>().data!!
        }

    suspend fun getPelanggan(): Result<List<PelangganDto>> =
        runCatching {
            val res = client.get("$BASE_URL/pelanggan") { accept(ContentType.Application.Json) }
            res.unwrapFlexible<List<PelangganDto>>().data!!
        }

    suspend fun getCustomers(): Result<List<Customer>> = getPelanggan().mapCatching { list -> list.map { it.toDomain() } }

    suspend fun getTotalPelanggan(): Result<Int> =
        runCatching {
            val res =
                client.get("$BASE_URL/pelanggan/hitung") {
                    accept(ContentType.Application.Json)
                }
            res.unwrapFlexible<TotalCustomer>().data!!.total
        }
}
