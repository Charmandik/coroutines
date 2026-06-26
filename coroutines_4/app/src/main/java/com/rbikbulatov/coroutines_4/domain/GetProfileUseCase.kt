package com.rbikbulatov.coroutines_4.domain

import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): UserProfile {
        return repository.fetchProfile()
    }
}
