package ru.bikbulatov.coroutines_5.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.bikbulatov.coroutines_5.data.local.FileManager
import ru.bikbulatov.coroutines_5.data.network.ApiService
import ru.bikbulatov.coroutines_5.data.network.model.PostResponse
import ru.bikbulatov.coroutines_5.data.network.model.UserResponse
import ru.bikbulatov.coroutines_5.domain.model.Post
import ru.bikbulatov.coroutines_5.domain.model.User
import ru.bikbulatov.coroutines_5.domain.model.UserProfile

class DataRepository(
    private val apiService: ApiService,
    private val fileManager: FileManager,
    private val scope: CoroutineScope
) {

    private var lastError: Throwable? = null
    private var dataLoaded: Boolean = false

    fun getLastError(): Throwable? = lastError

    fun resetState() {
        lastError = null
        dataLoaded = false
        apiService.setShouldFail(false)
        fileManager.setShouldFail(false)
    }

    fun isDataLoaded(): Boolean = dataLoaded

    suspend fun loadUserProfileScenario1(): UserProfile {
        var result: UserProfile? = null

        scope.launch {
            Log.d("DISPATCHER", "=== Scenario 1 START ===")
            Log.d("DISPATCHER", "Thread: ${Thread.currentThread().name}")
            
            val userResponse = apiService.fetchUser()
            val postsResponse = apiService.fetchUserPosts(userResponse.id)

            fileManager.saveCache("user_${userResponse.id}")
            
            Log.d("DISPATCHER", "=== Scenario 1 END ===")

            val user = User(
                id = userResponse.id,
                name = userResponse.name,
                email = userResponse.email
            )

            val posts = postsResponse.map { post ->
                Post(
                    id = post.id,
                    userId = post.userId,
                    title = post.title,
                    content = post.content
                )
            }

            result = UserProfile(
                user = user,
                posts = posts,
                cachedData = "cached"
            )
            dataLoaded = true
        }

        delay(300)

        if (!dataLoaded || result == null) {
            throw lastError ?: Exception("Unknown error")
        }

        return result
    }

    suspend fun loadUserProfileScenario2(): UserProfile {
        var result: UserProfile? = null

        scope.launch {
            Log.d("DISPATCHER", "=== Scenario 2 START ===")
            Log.d("DISPATCHER", "Thread: ${Thread.currentThread().name}")
            
            val userResponse = apiService.fetchUser()

            val postsResponse = try {
                apiService.fetchUserPosts(userResponse.id)
            } catch (e: Exception) {
                lastError = e
                emptyList()
            }

            val cachedData = fileManager.loadCache()
            
            Log.d("DISPATCHER", "=== Scenario 2 END ===")

            val user = User(
                id = userResponse.id,
                name = userResponse.name,
                email = userResponse.email
            )

            val posts = postsResponse.map { post ->
                Post(
                    id = post.id,
                    userId = post.userId,
                    title = post.title,
                    content = post.content
                )
            }

            result = UserProfile(
                user = user,
                posts = posts,
                cachedData = cachedData
            )
            dataLoaded = true
        }

        delay(300)

        if (!dataLoaded || result == null) {
            throw lastError ?: Exception("Unknown error")
        }

        return result
    }

    suspend fun loadUserProfileScenario3(): UserProfile {
        var result: UserProfile? = null

        scope.launch {
            Log.d("DISPATCHER", "=== Scenario 3 START ===")
            Log.d("DISPATCHER", "Thread: ${Thread.currentThread().name}")
            
            val userResponse = apiService.fetchUser()
            val postsResponse = apiService.fetchUserPosts(userResponse.id)

            fileManager.deleteOldFiles()
            
            Log.d("DISPATCHER", "=== Scenario 3 END ===")

            val user = User(
                id = userResponse.id,
                name = userResponse.name,
                email = userResponse.email
            )

            val posts = postsResponse.map { post ->
                Post(
                    id = post.id,
                    userId = post.userId,
                    title = post.title,
                    content = post.content
                )
            }

            result = UserProfile(
                user = user,
                posts = posts,
                cachedData = "refreshed"
            )
            dataLoaded = true
        }

        delay(300)

        if (!dataLoaded || result == null) {
            throw lastError ?: Exception("Unknown error")
        }

        return result
    }

    suspend fun loadUserProfileCorrect(): UserProfile = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val userResponse = apiService.fetchUser()
        val postsResponse = apiService.fetchUserPosts(userResponse.id)

        fileManager.saveCache("user_${userResponse.id}")

        val user = User(
            id = userResponse.id,
            name = userResponse.name,
            email = userResponse.email
        )

        val posts = postsResponse.map { post ->
            Post(
                id = post.id,
                userId = post.userId,
                title = post.title,
                content = post.content
            )
        }

        dataLoaded = true

        UserProfile(
            user = user,
            posts = posts,
            cachedData = "cached"
        )
    }
}
