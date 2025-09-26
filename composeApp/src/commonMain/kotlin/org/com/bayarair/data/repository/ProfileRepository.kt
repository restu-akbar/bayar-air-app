package org.com.bayarair.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import org.com.bayarair.data.dto.ProfileForm
import org.com.bayarair.data.dto.unwrapFlexible
import org.com.bayarair.data.model.User
import org.com.bayarair.data.remote.BASE_URL

class ProfileRepository(
    private val client: HttpClient,
) {
    // GET /user -> { status, message, data: {  } }
    suspend fun getUser(): Result<User> =
        runCatching {
            val res =
                client.get("$BASE_URL/user") {
                    accept(ContentType.Application.Json)
                }
            res.unwrapFlexible<User>().data!!
        }

    suspend fun updateProfile(
        form: ProfileForm,
        id: String,
    ): Result<User> =
        runCatching {
            val res =
                client.patch("$BASE_URL/profile/$id") {
                    setBody(ProfileForm(form.name, form.username, form.phone_number, form.email))
                }
            res.unwrapFlexible<User>().data!!
        }
}
