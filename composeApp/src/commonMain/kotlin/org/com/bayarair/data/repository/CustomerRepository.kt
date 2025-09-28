package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import org.com.bayarair.data.dto.PelangganDto
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.model.TotalCustomer
import org.com.bayarair.data.model.toDomain
import org.com.bayarair.data.remote.BASE_URL

class CustomerRepository(
    private val client: HttpClient,
) {

    suspend fun getPelanggan(): Result<List<PelangganDto>> =
        runCatching {
            val res = client.get("$BASE_URL/pelanggan") { accept(ContentType.Application.Json) }
            res.unwrapFlexible<List<PelangganDto>>().data!!
        }

    suspend fun getCustomers(): Result<List<Customer>> =
        getPelanggan().mapCatching { list -> list.map { it.toDomain() } }

    suspend fun getTotalPelanggan(): Result<Int> =
        runCatching {
            val res =
                client.get("$BASE_URL/pelanggan/hitung") {
                    accept(ContentType.Application.Json)
                }
            res.unwrapFlexible<TotalCustomer>().data!!.total
        }
}
