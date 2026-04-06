package com.sumread.data.local

interface SecretStore {
    suspend fun save(alias: String, value: String)
    suspend fun read(alias: String): String?
    suspend fun contains(alias: String): Boolean
    suspend fun delete(alias: String)
}
