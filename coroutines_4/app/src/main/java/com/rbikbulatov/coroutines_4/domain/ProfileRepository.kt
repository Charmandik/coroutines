package com.rbikbulatov.coroutines_4.domain

interface ProfileRepository {
    suspend fun fetchProfile(): UserProfile
}
