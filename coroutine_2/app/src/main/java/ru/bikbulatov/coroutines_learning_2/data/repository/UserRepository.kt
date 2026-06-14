package ru.bikbulatov.coroutines_learning_2.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.bikbulatov.coroutines_learning_2.data.network.ApiService
import ru.bikbulatov.coroutines_learning_2.data.network.model.PostNO
import ru.bikbulatov.coroutines_learning_2.data.network.model.SettingsNO
import ru.bikbulatov.coroutines_learning_2.data.network.model.UserNO
import ru.bikbulatov.coroutines_learning_2.domain.model.Post
import ru.bikbulatov.coroutines_learning_2.domain.model.Settings
import ru.bikbulatov.coroutines_learning_2.domain.model.User
import java.io.IOException

class UserRepository(
    private val apiService: ApiService,
    private val scope: CoroutineScope
) {

    private var lastError: Throwable? = null
    private var userDataLoaded: Boolean = false
    private var postsLoaded: Boolean = false
    private var settingsLoaded: Boolean = false

    fun getLastError(): Throwable? = lastError
    fun resetState() {
        lastError = null
        userDataLoaded = false
        postsLoaded = false
        settingsLoaded = false
    }

    fun isUserDataLoaded(): Boolean = userDataLoaded
    fun isPostsLoaded(): Boolean = postsLoaded
    fun isSettingsLoaded(): Boolean = settingsLoaded

    suspend fun getUserDataWithCatch(): Triple<User, List<Post>, Settings> {
        var caughtException: Exception? = null

        try {
            scope.launch {
                val userNO = apiService.fetchUser()
                val postsNO = apiService.fetchUserPosts(userNO.id)
                val settingsNO = apiService.fetchUserSettings(userNO.id)

                userDataLoaded = true
                postsLoaded = true
                settingsLoaded = true
            }
        } catch (e: Exception) {
            caughtException = e
            lastError = e
        }

        if (caughtException != null) {
            throw caughtException
        }

        delay(300)

        if (!userDataLoaded) {
            throw lastError ?: IOException("Unknown error")
        }

        return Triple(
            User(1, "John Doe", "john@example.com"),
            emptyList(),
            Settings(1, "dark", false)
        )
    }

    suspend fun getUserDataWithNestedLaunch(): Triple<User, List<Post>, Settings> {
        var caughtException: Exception? = null

        scope.launch {
            try {
                launch {
                    val userNO = apiService.fetchUser()
                    val postsNO = apiService.fetchUserPosts(userNO.id)
                    val settingsNO = apiService.fetchUserSettings(userNO.id)

                    userDataLoaded = true
                    postsLoaded = true
                    settingsLoaded = true
                }
            } catch (e: Exception) {
                caughtException = e
                lastError = e
            }
        }.join()

        if (caughtException != null) {
            throw caughtException
        }

        if (!userDataLoaded) {
            throw lastError ?: IOException("Unknown error")
        }

        return Triple(
            User(1, "John Doe", "john@example.com"),
            emptyList(),
            Settings(1, "dark", false)
        )
    }

    suspend fun getUserDataWithAsync(): Triple<User, List<Post>, Settings> {
        var caughtException: Exception? = null

        try {
            val userDeferred = scope.async {
                val userNO = apiService.fetchUser()
                userNO.toDomain()
            }

            val postsDeferred = scope.async {
                val user = userDeferred.await()
                val postsNO = apiService.fetchUserPosts(user.id)
                postsNO.map { it.toDomain() }
            }

            val settingsDeferred = scope.async {
                val user = userDeferred.await()
                val settingsNO = apiService.fetchUserSettings(user.id)
                settingsNO.toDomain()
            }

            val user = userDeferred.await()
            val posts = postsDeferred.await()
            val settings = settingsDeferred.await()

            userDataLoaded = true
            postsLoaded = true
            settingsLoaded = true

            return Triple(user, posts, settings)
        } catch (e: Exception) {
            caughtException = e
            lastError = e
        }

        if (caughtException != null) {
            throw caughtException
        }

        return Triple(
            User(1, "John Doe", "john@example.com"),
            emptyList(),
            Settings(1, "dark", false)
        )
    }

    suspend fun getUserDataCorrect(): Triple<User, List<Post>, Settings> = withContext(Dispatchers.IO) {
        val userNO = apiService.fetchUser()
        val postsNO = apiService.fetchUserPosts(userNO.id)
        val settingsNO = apiService.fetchUserSettings(userNO.id)

        val user = userNO.toDomain()
        val posts = postsNO.map { it.toDomain() }
        val settings = settingsNO.toDomain()

        userDataLoaded = true
        postsLoaded = true
        settingsLoaded = true

        Triple(user, posts, settings)
    }
}
