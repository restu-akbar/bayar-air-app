package org.com.bayarair.shared.home

data class HomeState(
    val greeting: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)
