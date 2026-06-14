package ru.bikbulatov.coroutines_3.data

class ProfileRepository(
    private val api: ProfileApi = ProfileApi()
) {
    suspend fun loadHeader(): String? {
        return api.loadHeader()
    }

    suspend fun loadBody(): String? {
        return api.loadBody()
    }

    suspend fun loadFooter(): String? {
        return api.loadFooter()
    }
}