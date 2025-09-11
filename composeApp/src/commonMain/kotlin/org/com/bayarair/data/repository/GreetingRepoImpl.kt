package org.com.bayarair.data.repository

class GreetingRepoImpl : GreetingRepo {
    override suspend fun greet(): String = "Hello from GreetingRepo (KMP)!"
}
