package ru.bikbulatov.coroutines_5.data.network

import kotlinx.coroutines.delay
import ru.bikbulatov.coroutines_5.data.network.model.UserResponse
import ru.bikbulatov.coroutines_5.data.network.model.PostResponse
import java.io.IOException

class ApiService {

    private var shouldFail = false

    fun setShouldFail(shouldFail: Boolean) {
        this.shouldFail = shouldFail
    }

    suspend fun fetchUser(): UserResponse {
        delay(100)
        if (shouldFail) {
            throw IOException("Network error: Unable to fetch user")
        }
        return UserResponse(
            id = 1,
            name = "John Doe",
            email = "john@example.com"
        )
    }

    suspend fun fetchUserPosts(userId: Int): List<PostResponse> {
        delay(100)
        if (shouldFail) {
            throw IOException("Network error: Unable to fetch posts")
        }
        return listOf(
            PostResponse(id = 1, userId = userId, title = "Post 1", content = "Content 1"),
            PostResponse(id = 2, userId = userId, title = "Post 2", content = "Content 2")
        )
    }
}
