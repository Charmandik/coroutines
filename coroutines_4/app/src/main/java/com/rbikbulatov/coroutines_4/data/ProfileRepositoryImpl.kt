package com.rbikbulatov.coroutines_4.data

import com.rbikbulatov.coroutines_4.domain.ProfileRepository
import com.rbikbulatov.coroutines_4.domain.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {

    override suspend fun fetchProfile(): UserProfile {
        delay(1500)
        return UserProfile(
            name = "Андрей",
            bio = "Android Developer"
        )
    }
}
