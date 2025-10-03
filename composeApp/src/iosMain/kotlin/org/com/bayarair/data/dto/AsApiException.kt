package org.com.bayarair.data.dto

import platform.Foundation.NSError
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut

actual fun Throwable.asApiException(): ApiException =
    when (this) {
        is ApiException -> this

        is NSError ->
            when (code) {
                NSURLErrorNotConnectedToInternet ->
                    ApiException(
                        CODE_NETWORK_UNAVAILABLE,
                        "Tidak ada koneksi internet.",
                    )

                NSURLErrorTimedOut ->
                    ApiException(
                        CODE_TIMEOUT,
                        "Timeout.",
                    )

                else -> ApiException(CODE_UNKNOWN, localizedDescription)
            }

        else -> ApiException(CODE_UNKNOWN, message ?: "Kesalahan tak dikenal.")
    }
