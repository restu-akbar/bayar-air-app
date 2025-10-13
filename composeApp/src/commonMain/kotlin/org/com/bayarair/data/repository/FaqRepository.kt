package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import org.com.bayarair.data.dto.Faq
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.remote.BASE_URL

class FaqRepository(
    private val client: HttpClient,
) {
    suspend fun getFaq(): Result<List<Faq>> =
        runCatching {
            val res =
                client.get("$BASE_URL/faq") {
                    accept(ContentType.Application.Json)
                }
            res.unwrapFlexible<List<Faq>>().data!!
        }
}
