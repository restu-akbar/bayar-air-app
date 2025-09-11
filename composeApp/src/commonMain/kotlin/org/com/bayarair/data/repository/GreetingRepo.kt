package org.com.bayarair.data.repository

interface GreetingRepo {
    suspend fun greet(): String
}
