package org.com.bayarair.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class Faq(
    val question: String,
    val answer: String,
)
