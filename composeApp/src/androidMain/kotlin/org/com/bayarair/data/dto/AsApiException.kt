package org.com.bayarair.data.dto

actual fun Throwable.asApiException(): ApiException = when (this) {
    is ApiException -> this

    is java.net.UnknownHostException,
    is java.net.ConnectException,
    is java.nio.channels.UnresolvedAddressException -> ApiException(
        CODE_NETWORK_UNAVAILABLE,
        "Tidak ada koneksi internet."
    )

    is java.net.SocketTimeoutException,
    is kotlinx.coroutines.TimeoutCancellationException -> ApiException(
        CODE_TIMEOUT,
        "Timeout. Server lambat atau jaringan putus."
    )

    is javax.net.ssl.SSLException -> ApiException(
        CODE_UNKNOWN,
        "Masalah SSL/sertifikat."
    )

    is kotlinx.coroutines.CancellationException -> ApiException(
        CODE_CANCELED,
        "Permintaan dibatalkan."
    )

    else -> ApiException(CODE_UNKNOWN, message ?: "Kesalahan tak dikenal")
}
