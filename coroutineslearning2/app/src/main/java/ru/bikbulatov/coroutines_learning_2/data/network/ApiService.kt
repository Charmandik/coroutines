package ru.bikbulatov.coroutines_learning_2.data.network

import kotlinx.coroutines.delay
import ru.bikbulatov.coroutines_learning_2.data.network.model.PostNO
import ru.bikbulatov.coroutines_learning_2.data.network.model.SettingsNO
import ru.bikbulatov.coroutines_learning_2.data.network.model.UserNO
import java.io.IOException
import java.net.SocketTimeoutException

class ApiService {

    private var shouldFailUser = false
    private var shouldFailPosts = false
    private var shouldFailSettings = false

    fun setFailUser(shouldFail: Boolean) {
        shouldFailUser = shouldFail
    }

    fun setFailPosts(shouldFail: Boolean) {
        shouldFailPosts = shouldFail
    }

    fun setFailSettings(shouldFail: Boolean) {
        shouldFailSettings = shouldFail
    }

    suspend fun fetchUser(): UserNO {
        delay(100)
        if (shouldFailUser) {
            throw IOException("Network error: Unable to fetch user")
        }
        return UserNO(
            id = 1,
            name = "John Doe",
            email = "john@example.com"
        )
    }

    suspend fun fetchUserPosts(userId: Int): List<PostNO> {
        delay(100)
        if (shouldFailPosts) {
            throw HttpException(
                code = 500,
                message = "Internal Server Error"
            )
        }
        return listOf(
            PostNO(id = 1, userId = userId, title = "Post 1", body = "Content 1"),
            PostNO(id = 2, userId = userId, title = "Post 2", body = "Content 2")
        )
    }

    suspend fun fetchUserSettings(userId: Int): SettingsNO {
        delay(100)
        if (shouldFailSettings) {
            throw SocketTimeoutException("Connection timed out")
        }
        return SettingsNO(
            userId = userId,
            theme = "dark",
            notificationsEnabled = true
        )
    }

    class HttpException(
        val code: Int,
        message: String
    ) : Exception("HTTP $code: $message")
}
