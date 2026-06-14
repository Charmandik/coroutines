package ru.bikbulatov.coroutines_3.domain

import ru.bikbulatov.coroutines_3.data.ProfileRepository

class ProfileInteractor(
    val repository: ProfileRepository
) {
    suspend fun loadHeader(): String? {
        return repository.loadHeader()
    }

    suspend fun loadBody(): String? {
        return repository.loadBody()
    }

    suspend fun loadFooter(): String? {
        return repository.loadFooter()
    }
}